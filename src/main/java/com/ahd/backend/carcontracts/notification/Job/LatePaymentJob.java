package com.ahd.backend.carcontracts.notification.job;

import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import com.ahd.backend.carcontracts.payment.repository.InstallmentRepository;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LatePaymentJob {

    private final InstallmentRepository installmentRepo;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Runs daily at 9:00 AM to check for late installments.
     * Sends notification to ALL users in companies with late payments
     */
    @Scheduled(cron = "0 00 23 * * ?") // 11:00 PM daily
    @Transactional
    public void checkForLatePayments() {
        log.info("Starting scheduled job: Check Late Payments at {}", getCurrentBaghdadTime());
        
        LocalDate today = LocalDate.now();
        
        try {
            // Find companies with late installments
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
                    sendNotificationsToAllCompanyUsers(companyId, overdueCount);
                }
            }
            
            log.info("Completed scheduled job: Check Late Payments");
        } catch (Exception e) {
            log.error("Error in LatePaymentJob: ", e);
        }
    }
    
    private void sendNotificationsToAllCompanyUsers(Long companyId, Long overdueCount) {
        try {
            // Get ALL user IDs for this company using the correct repository method
            List<Long> allUserIds = userRepository.findUserIdsByCompanyId(companyId);
            
            if (allUserIds == null || allUserIds.isEmpty()) {
                log.warn("No users found for company {}", companyId);
                return;
            }
            
            log.info("Sending late payment notification to {} users in company {}", 
                     allUserIds.size(), companyId);
            
            // Prepare additional data (optional, if you need to add to notification)
            Map<String, String> additionalData = new HashMap<>();
            additionalData.put("overdueCount", String.valueOf(overdueCount));
            additionalData.put("companyId", String.valueOf(companyId));
            additionalData.put("alertType", "LATE_PAYMENT");
            additionalData.put("date", LocalDate.now().toString());
            
            // Create notification request for ALL company users
            NotificationRequest request = NotificationRequest.builder()
                    .title("⚠️ تنبيه: دفعات متأخرة")
                    .message(String.format("لدى شركتك %d دفعة(دفعات) متأخرة تحتاج إلى متابعة فورية.", overdueCount))
                    .actionBy("SYSTEM")
                    .actionType("LATE_PAYMENT_ALERT")
                    .actionDate(getCurrentBaghdadTime())
                    .companyId(companyId)
                    .targetUserIds(allUserIds) // Send to ALL users in the company
                    .additionalData(additionalData)
                    .build();
            
            // Send notifications (this will create one notification per user)
            notificationService.sendNotification(request);
            
            log.info("Successfully sent {} late payment alerts to company {}", 
                     allUserIds.size(), companyId);
            
        } catch (Exception e) {
            log.error("Error sending notifications to company {}: {}", companyId, e.getMessage(), e);
        }
    }
    
    private LocalDateTime getCurrentBaghdadTime() {
        return ZonedDateTime.now(ZoneId.of("Asia/Baghdad")).toLocalDateTime();
    }
}