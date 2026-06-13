package com.ahd.backend.carcontracts.notification.job;

import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompanyExpirationJob {

    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Runs daily at 7:35 PM to check for expiring company subscriptions
     */
    @Scheduled(cron = "0 00 23 * * ?") // 11:00 PM daily
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
    
    private int checkAndSendCompanyNotifications(Company company, LocalDate today) {
        LocalDate expirationDate = company.getExpirationDate();
        
        if (expirationDate == null) {
            log.debug("Company {} has no expiration date", company.getId());
            return 0;
        }
        
        long daysUntilExpiration = ChronoUnit.DAYS.between(today, expirationDate);
        
        log.debug("Company: {} (ID: {}), Expiration: {}, Days left: {}", 
                 company.getCompanyName(), company.getId(), expirationDate, daysUntilExpiration);
        
        // Get SUPER_ADMIN users (system-wide)
        List<Long> superAdminUserIds = userRepository.findUserIdsByRole("ROLE_SUPER_ADMIN");
        
        // Get company users (users belonging to this company)
        List<Long> companyUserIds = userRepository.findUserIdsByCompanyId(company.getId());
        
        // Combine recipients
        List<Long> allRecipients = new ArrayList<>();
        allRecipients.addAll(superAdminUserIds);
        allRecipients.addAll(companyUserIds);
        
        // Remove duplicates if any
        allRecipients = allRecipients.stream().distinct().collect(java.util.stream.Collectors.toList());
        
        if (allRecipients.isEmpty()) {
            log.warn("No recipients found for company {} expiration", company.getId());
            return 0;
        }
        
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("companyId", String.valueOf(company.getId()));
        additionalData.put("companyName", company.getCompanyName());
        additionalData.put("expirationDate", expirationDate.toString());
        additionalData.put("daysUntilExpiration", String.valueOf(daysUntilExpiration));
        additionalData.put("ownerName", company.getOwnerName());
        additionalData.put("ownerContact", company.getOwnerContact());
        
        // 1. Check if EXPIRED
        if (expirationDate.isBefore(today)) {
            String title = "🚨 انتهاء اشتراك الشركة";
            String message = String.format(
                "انتهى اشتراك شركة %s بتاريخ %s. يرجى تجديد الاشتراك فوراً.",
                company.getCompanyName(),
                expirationDate
            );
            
            sendNotificationToUsers(allRecipients, company.getId(), title, message, 
                                   "COMPANY_SUBSCRIPTION_EXPIRED", additionalData);
            
            log.info("✅ Sent EXPIRED notification for company {}", company.getId());
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
            
            sendNotificationToUsers(allRecipients, company.getId(), title, message, 
                                   "COMPANY_SUBSCRIPTION_EXPIRING_SOON", additionalData);
            
            log.info("✅ Sent EXPIRING SOON ({} days) notification for company {}", 
                     daysUntilExpiration, company.getId());
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
            
            sendNotificationToUsers(allRecipients, company.getId(), title, message, 
                                   "COMPANY_SUBSCRIPTION_EXPIRING", additionalData);
            
            log.info("✅ Sent EXPIRING ({} days) notification for company {}", 
                     daysUntilExpiration, company.getId());
            return 1;
        }
        
        log.debug("No notification needed for company {} (expires in {} days)", 
                  company.getId(), daysUntilExpiration);
        return 0;
    }
    
    private void sendNotificationToUsers(List<Long> userIds, Long companyId, 
                                         String title, String message, 
                                         String actionType, Map<String, String> additionalData) {
        try {
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
            log.debug("Notification sent to {} users for company {}", userIds.size(), companyId);
            
        } catch (Exception e) {
            log.error("Error sending company expiration notification: {}", e.getMessage(), e);
        }
    }
    
    private LocalDateTime getCurrentBaghdadTime() {
        return ZonedDateTime.now(ZoneId.of("Asia/Baghdad")).toLocalDateTime();
    }
}