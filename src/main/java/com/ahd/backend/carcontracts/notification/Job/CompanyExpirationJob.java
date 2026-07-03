package com.ahd.backend.carcontracts.notification.job;

import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompanyExpirationJob {

    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Runs daily at 11:00 PM to check for expiring company subscriptions
     */
    @Scheduled(cron = "0 21 20 * * ?") // 11:00 PM daily
    @Transactional
    public void checkCompanyExpirations() {
        log.info("========== STARTING COMPANY EXPIRATION JOB ==========");
        log.info("Job started at: {}", getCurrentBaghdadTime());
        
        try {
            LocalDate today = LocalDate.now();
            
            // Find all active companies
            List<Company> allCompanies = companyRepository.findByDeletedFalse();
            
            if (allCompanies == null || allCompanies.isEmpty()) {
                log.info("No companies found to check for expirations");
                return;
            }
            
            log.info("Checking {} companies for subscription expirations", allCompanies.size());
            
            int totalNotificationsSent = 0;
            
            for (Company company : allCompanies) {
                int notificationsForCompany = checkAndSendCompanyNotifications(company, today);
                totalNotificationsSent += notificationsForCompany;
            }
            
            log.info("Completed Company Expiration Job - Total notifications sent: {}", totalNotificationsSent);
            log.info("========== COMPANY EXPIRATION JOB COMPLETED ==========");
            
        } catch (Exception e) {
            log.error("Error in CompanyExpirationJob: ", e);
        }
    }
    
    /**
     * Check if user has GET_NOTIFICATIONS permission
     */
    private boolean hasGetNotificationsPermission(AppUser user) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        
        // Check through user's roles and their permissions
        return user.getRoles().stream()
            .filter(role -> role.getPermissions() != null)
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(permission -> "GET_NOTIFICATIONS".equals(permission.getName()));
    }

    /**
     * Filter users by GET_NOTIFICATIONS permission and valid FCM token
     */
    private List<AppUser> filterUsersWithNotificationPermission(List<AppUser> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }
        
        return users.stream()
            .filter(user -> user.getFcmToken() != null && !user.getFcmToken().isEmpty())
            .filter(this::hasGetNotificationsPermission)
            .collect(Collectors.toList());
    }

    /**
     * Get Super Admin users with GET_NOTIFICATIONS permission
     */
    private List<Long> getAuthorizedSuperAdminUserIds() {
        List<AppUser> superAdminUsers = userRepository.findUsersByRole("ROLE_SUPER_ADMIN");
        List<AppUser> authorizedSuperAdmins = filterUsersWithNotificationPermission(superAdminUsers);
        
        return authorizedSuperAdmins.stream()
            .map(AppUser::getId)
            .collect(Collectors.toList());
    }

    /**
     * Get company users with GET_NOTIFICATIONS permission
     */
    private List<Long> getAuthorizedCompanyUserIds(Long companyId) {
        List<AppUser> companyUsers = userRepository.findByCompanyIdAndFcmTokenIsNotNull(companyId);
        List<AppUser> authorizedUsers = filterUsersWithNotificationPermission(companyUsers);
        
        return authorizedUsers.stream()
            .map(AppUser::getId)
            .collect(Collectors.toList());
    }

    /**
     * Get all authorized recipients (Super Admins + Company Users) with GET_NOTIFICATIONS permission
     */
    private List<Long> getAllAuthorizedRecipients(Long companyId) {
        Set<Long> allRecipients = new HashSet<>();
        
        // Add authorized Super Admins
        allRecipients.addAll(getAuthorizedSuperAdminUserIds());
        
        // Add authorized company users
        allRecipients.addAll(getAuthorizedCompanyUserIds(companyId));
        
        return new ArrayList<>(allRecipients);
    }
    
    private int checkAndSendCompanyNotifications(Company company, LocalDate today) {
        LocalDate expirationDate = company.getExpirationDate();
        
        if (expirationDate == null) {
            log.debug("Company {} has no expiration date", company.getId());
            return 0;
        }
        
        long daysUntilExpiration = ChronoUnit.DAYS.between(today, expirationDate);
        long daysSinceExpiration = ChronoUnit.DAYS.between(expirationDate, today);
        
        log.debug("Company: {} (ID: {}), Expiration: {}, Days left: {}, Days since expiration: {}", 
                 company.getCompanyName(), company.getId(), expirationDate, daysUntilExpiration, daysSinceExpiration);
        
        // ✅ Get only users with GET_NOTIFICATIONS permission (Super Admins + Company Users)
        List<Long> authorizedRecipients = getAllAuthorizedRecipients(company.getId());
        
        if (authorizedRecipients.isEmpty()) {
            log.info("No users with GET_NOTIFICATIONS permission found for company {} expiration", company.getId());
            return 0;
        }
        
        log.debug("Found {} authorized recipients for company {} (with GET_NOTIFICATIONS permission)", 
                  authorizedRecipients.size(), company.getId());
        
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("companyId", String.valueOf(company.getId()));
        additionalData.put("companyName", company.getCompanyName());
        additionalData.put("expirationDate", expirationDate.toString());
        additionalData.put("daysUntilExpiration", String.valueOf(daysUntilExpiration));
        additionalData.put("ownerName", company.getOwnerName() != null ? company.getOwnerName() : "");
        additionalData.put("ownerContact", company.getOwnerContact() != null ? company.getOwnerContact() : "");
        
        // 1. Check if company is expired within the last 10 days
        if (daysSinceExpiration >= 0 && daysSinceExpiration <= 10 && expirationDate.isBefore(today)) {
            // Send notification for each day in the last 10 days
            int dayNumber = (int) daysSinceExpiration + 1; // 1 = first day expired, 10 = 10th day expired
            
            String title = "🚨 انتهاء اشتراك الشركة - اليوم " + dayNumber;
            String message = String.format(
                "انتهى اشتراك شركة %s منذ %d يوم (تاريخ الانتهاء: %s). يرجى تجديد الاشتراك فوراً.",
                company.getCompanyName(),
                daysSinceExpiration,
                expirationDate
            );
            
            sendNotificationToUsers(authorizedRecipients, company.getId(), title, message, 
                                   "COMPANY_SUBSCRIPTION_EXPIRED", additionalData);
            
            log.info("✅ Sent EXPIRED notification (Day {} of 10) for company {} to {} authorized users", 
                     dayNumber, company.getId(), authorizedRecipients.size());
            return 1;
        }
        
        // 2. Check if EXPIRING WITHIN 7 DAYS
        else if (daysUntilExpiration >= 0 && daysUntilExpiration <= 7) {
            String title = "⚠️ تنبيه: اشتراك الشركة على وشك الانتهاء";
            String message = String.format(
                "ينتهي اشتراك شركة %s خلال %d يوم (تاريخ الانتهاء: %s). يرجى تجديد الاشتراك.",
                company.getCompanyName(),
                daysUntilExpiration,
                expirationDate
            );
            
            sendNotificationToUsers(authorizedRecipients, company.getId(), title, message, 
                                   "COMPANY_SUBSCRIPTION_EXPIRING_SOON", additionalData);
            
            log.info("✅ Sent EXPIRING SOON ({} days) notification for company {} to {} authorized users", 
                     daysUntilExpiration, company.getId(), authorizedRecipients.size());
            return 1;
        }
        
        // 3. Check if EXPIRING WITHIN 30 DAYS
        else if (daysUntilExpiration > 7 && daysUntilExpiration <= 30) {
            String title = "📅 تذكير: اشتراك الشركة على وشك الانتهاء";
            String message = String.format(
                "سينتهي اشتراك شركة %s خلال %d يوم (تاريخ الانتهاء: %s). يرجى التخطيط للتجديد.",
                company.getCompanyName(),
                daysUntilExpiration,
                expirationDate
            );
            
            sendNotificationToUsers(authorizedRecipients, company.getId(), title, message, 
                                   "COMPANY_SUBSCRIPTION_EXPIRING", additionalData);
            
            log.info("✅ Sent EXPIRING ({} days) notification for company {} to {} authorized users", 
                     daysUntilExpiration, company.getId(), authorizedRecipients.size());
            return 1;
        }
        
        log.debug("No notification needed for company {} (expires in {} days, expired {} days ago)", 
                  company.getId(), daysUntilExpiration, daysSinceExpiration);
        return 0;
    }
    
    private void sendNotificationToUsers(List<Long> userIds, Long companyId, 
                                         String title, String message, 
                                         String actionType, Map<String, String> additionalData) {
        try {
            if (userIds == null || userIds.isEmpty()) {
                log.debug("No authorized users to send company expiration notification for company {}", companyId);
                return;
            }
            
            NotificationRequest request = NotificationRequest.builder()
                    .title(title)
                    .message(message)
                    .actionBy("SYSTEM")
                    .actionType(actionType)
                    .actionDate(getCurrentBaghdadTime())
                    .companyId(companyId)
                    .targetUserIds(userIds)
                    .additionalData(additionalData)
                    .build();
            
            notificationService.sendNotification(request);
            log.debug("Notification sent to {} authorized users for company {}", userIds.size(), companyId);
            
        } catch (Exception e) {
            log.error("Error sending company expiration notification: {}", e.getMessage(), e);
        }
    }
    
    private LocalDateTime getCurrentBaghdadTime() {
        return ZonedDateTime.now(ZoneId.of("Asia/Baghdad")).toLocalDateTime();
    }
}