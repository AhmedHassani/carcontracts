package com.ahd.backend.carcontracts.notification.job;

import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import com.ahd.backend.carcontracts.payment.repository.InstallmentRepository;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.appuser.models.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LatePaymentJob {

    private final InstallmentRepository installmentRepo;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 00 23 * * ?")
    @Transactional
    public void checkForLatePayments() {
        log.info("Starting scheduled job: Check Late Payments at {}", getCurrentBaghdadTime());
        
        LocalDate today = LocalDate.now();
        
        try {
            List<Object[]> results = installmentRepo.countOverdueInstallmentsByCompany(
                    today,
                    List.of(InstallmentStatus.PAID, InstallmentStatus.CANCELLED));
            
            if (results == null || results.isEmpty()) {
                log.info("No overdue installments found");
                return;
            }
            
            log.info("Found {} companies with overdue installments", results.size());
            
            for (Object[] result : results) {
                Long companyId = (Long) result[0];
                Long overdueCount = (Long) result[1];
                
                if (overdueCount != null && overdueCount > 0) {
                    sendNotificationsToAuthorizedCompanyUsers(companyId, overdueCount);
                }
            }
            
            log.info("Completed scheduled job: Check Late Payments");
        } catch (Exception e) {
            log.error("Error in LatePaymentJob: ", e);
        }
    }
    
    private boolean hasGetNotificationsPermission(AppUser user) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        
        return user.getRoles().stream()
            .filter(role -> role.getPermissions() != null)
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(permission -> "GET_NOTIFICATIONS".equals(permission.getName()));
    }

    private List<AppUser> filterUsersWithNotificationPermission(List<AppUser> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }
        
        return users.stream()
            .filter(user -> user.getFcmToken() != null && !user.getFcmToken().isEmpty())
            .filter(this::hasGetNotificationsPermission)
            .collect(Collectors.toList());
    }

    private List<Long> getAuthorizedCompanyUserIds(Long companyId) {
        List<AppUser> companyUsers = userRepository.findByCompanyIdAndFcmTokenIsNotNull(companyId);
        List<AppUser> authorizedUsers = filterUsersWithNotificationPermission(companyUsers);
        
        return authorizedUsers.stream()
            .map(AppUser::getId)
            .collect(Collectors.toList());
    }
    
    private void sendNotificationsToAuthorizedCompanyUsers(Long companyId, Long overdueCount) {
        try {
            List<Long> authorizedUserIds = getAuthorizedCompanyUserIds(companyId);
            
            if (authorizedUserIds == null || authorizedUserIds.isEmpty()) {
                log.info("No users with GET_NOTIFICATIONS permission found for company {}", companyId);
                return;
            }
            
            log.info("Sending late payment notification to {} authorized users in company {}", 
                     authorizedUserIds.size(), companyId);
            
            Map<String, String> additionalData = new HashMap<>();
            additionalData.put("overdueCount", String.valueOf(overdueCount));
            additionalData.put("companyId", String.valueOf(companyId));
            additionalData.put("alertType", "LATE_PAYMENT");
            additionalData.put("date", LocalDate.now().toString());
            
            NotificationRequest request = NotificationRequest.builder()
                    .title("⚠️ تنبيه: دفعات متأخرة")
                    .message(String.format("لدى شركتك %d دفعة(دفعات) متأخرة تحتاج إلى متابعة فورية.", overdueCount))
                    .actionBy("SYSTEM")
                    .actionType("LATE_PAYMENT_ALERT")
                    .actionDate(getCurrentBaghdadTime())
                    .companyId(companyId)
                    .targetUserIds(authorizedUserIds)
                    .additionalData(additionalData)
                    .build();
            
            notificationService.sendNotification(request);
            
            log.info("Successfully sent {} late payment alerts to company {}", 
                     authorizedUserIds.size(), companyId);
            
        } catch (Exception e) {
            log.error("Error sending late payment notifications to company {}: {}", companyId, e.getMessage(), e);
        }
    }
    
    private LocalDateTime getCurrentBaghdadTime() {
        return ZonedDateTime.now(ZoneId.of("Asia/Baghdad")).toLocalDateTime();
    }
}