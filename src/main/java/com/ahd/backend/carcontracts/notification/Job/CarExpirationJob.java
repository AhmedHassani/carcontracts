package com.ahd.backend.carcontracts.notification.job;

import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.repository.CarRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarExpirationJob {

    private final CarRepository carRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Runs daily at 7:25 PM to check for expiring annual contracts and inspections
     * (Same time as LatePaymentJob to keep consistency)
     */
    @Scheduled(cron = "0 00 23 * * ?") // 11:00 PM daily
    @Transactional
    public void checkCarExpirations() {
        log.info("========== STARTING CAR EXPIRATION JOB ==========");
        log.info("Job started at: {}", getCurrentBaghdadTime());
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);
            LocalDate nextMonth = today.plusDays(30);
            
            log.info("Checking dates - Today: {}, Next Week: {}, Next Month: {}", today, nextWeek, nextMonth);
            
            // Find all active cars
            List<Car> allCars = carRepository.findByDeletedFalse();
            
            if (allCars == null || allCars.isEmpty()) {
                log.info("No cars found to check for expirations");
                return;
            }
            
            log.info("Checking {} cars for expirations", allCars.size());
            
            int totalNotificationsSent = 0;
            
            // Process each car individually
            for (Car car : allCars) {
                int notificationsForCar = checkAndSendCarNotifications(car, today, nextWeek, nextMonth);
                totalNotificationsSent += notificationsForCar;
            }
            
            log.info("Completed Car Expiration Job - Total notifications sent: {}", totalNotificationsSent);
            log.info("========== CAR EXPIRATION JOB COMPLETED ==========");
            
        } catch (Exception e) {
            log.error("Error in CarExpirationJob: ", e);
        }
    }
    
    private int checkAndSendCarNotifications(Car car, LocalDate today, 
                                              LocalDate nextWeek, LocalDate nextMonth) {
        int notificationCount = 0;
        
        // Log car being checked
        log.debug("Checking car ID: {}, Name: {}, Company: {}", car.getId(), car.getName(), car.getCompanyId());
        
        // 1. Check Annual Contract Expiration
        if (car.getAnnualContractDate() != null) {
            log.debug("Car {} has annual contract date: {}", car.getId(), car.getAnnualContractDate());
            boolean sent = checkAnnualContractExpiration(car, today, nextWeek, nextMonth);
            if (sent) notificationCount++;
        } else {
            log.debug("Car {} has no annual contract date", car.getId());
        }
        
        // 2. Check Inspection Date Expiration
        if (car.getInspectionDate() != null) {
            log.debug("Car {} has inspection date: {}", car.getId(), car.getInspectionDate());
            boolean sent = checkInspectionExpiration(car, today, nextWeek, nextMonth);
            if (sent) notificationCount++;
        } else {
            log.debug("Car {} has no inspection date", car.getId());
        }
        
        return notificationCount;
    }
    
    private boolean checkAnnualContractExpiration(Car car, LocalDate today, 
                                                   LocalDate nextWeek, LocalDate nextMonth) {
        LocalDate contractDate = car.getAnnualContractDate();
        long daysUntilExpiration = ChronoUnit.DAYS.between(today, contractDate);
        
        log.debug("Annual contract for car {} expires in {} days", car.getId(), daysUntilExpiration);
        
        // Get all company users
        List<Long> allUserIds = userRepository.findUserIdsByCompanyId(car.getCompanyId());
        if (allUserIds.isEmpty()) {
            log.warn("No users found for company {}", car.getCompanyId());
            return false;
        }
        
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("carId", String.valueOf(car.getId()));
        additionalData.put("carName", car.getName());
        additionalData.put("plateNumber", car.getPlateNumber() != null ? car.getPlateNumber() : "N/A");
        additionalData.put("expirationDate", contractDate.toString());
        additionalData.put("daysUntilExpiration", String.valueOf(daysUntilExpiration));
        
        // Check if EXPIRED (contract date is before today)
        if (contractDate.isBefore(today)) {
            String message = String.format(
                "🚨 انتهت صلاحية العقد السنوي للسيارة %s (لوحة: %s) بتاريخ %s. يرجى التجديد فوراً.",
                car.getName(),
                car.getPlateNumber() != null ? car.getPlateNumber() : "بدون لوحة",
                contractDate
            );
            
            sendNotificationToUsers(car.getCompanyId(), allUserIds, 
                "❌ انتهاء العقد السنوي للسيارة", 
                message, 
                "ANNUAL_CONTRACT_EXPIRED",
                additionalData);
            
            log.info("✅ Sent EXPIRED notification for car {} annual contract", car.getId());
            return true;
        }
        
        // Check if EXPIRING WITHIN 7 DAYS
        else if (daysUntilExpiration >= 0 && daysUntilExpiration <= 7) {
            String message = String.format(
                "⚠️ ينتهي العقد السنوي للسيارة %s (لوحة: %s) خلال %d يوم (تاريخ الانتهاء: %s). يرجى تجديد العقد.",
                car.getName(),
                car.getPlateNumber() != null ? car.getPlateNumber() : "بدون لوحة",
                daysUntilExpiration,
                contractDate
            );
            
            sendNotificationToUsers(car.getCompanyId(), allUserIds, 
                "⚠️ تنبيه: اقتراب انتهاء العقد السنوي", 
                message, 
                "ANNUAL_CONTRACT_EXPIRING_SOON",
                additionalData);
            
            log.info("✅ Sent EXPIRING SOON ({} days) notification for car {} annual contract", 
                     daysUntilExpiration, car.getId());
            return true;
        }
        
        // Check if EXPIRING WITHIN 30 DAYS
        else if (daysUntilExpiration > 7 && daysUntilExpiration <= 30) {
            String message = String.format(
                "📅 سينتهي العقد السنوي للسيارة %s (لوحة: %s) خلال %d يوم (تاريخ الانتهاء: %s). يرجى التخطيط للتجديد.",
                car.getName(),
                car.getPlateNumber() != null ? car.getPlateNumber() : "بدون لوحة",
                daysUntilExpiration,
                contractDate
            );
            
            sendNotificationToUsers(car.getCompanyId(), allUserIds, 
                "📅 تذكير: العقد السنوي على وشك الانتهاء", 
                message, 
                "ANNUAL_CONTRACT_EXPIRING",
                additionalData);
            
            log.info("✅ Sent EXPIRING ({} days) notification for car {} annual contract", 
                     daysUntilExpiration, car.getId());
            return true;
        }
        
        log.debug("No notification needed for car {} annual contract (expires in {} days)", 
                  car.getId(), daysUntilExpiration);
        return false;
    }
    
    private boolean checkInspectionExpiration(Car car, LocalDate today, 
                                              LocalDate nextWeek, LocalDate nextMonth) {
        LocalDate inspectionDate = car.getInspectionDate();
        long daysUntilExpiration = ChronoUnit.DAYS.between(today, inspectionDate);
        
        log.debug("Inspection for car {} expires in {} days", car.getId(), daysUntilExpiration);
        
        // Get all company users
        List<Long> allUserIds = userRepository.findUserIdsByCompanyId(car.getCompanyId());
        if (allUserIds.isEmpty()) {
            log.warn("No users found for company {}", car.getCompanyId());
            return false;
        }
        
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("carId", String.valueOf(car.getId()));
        additionalData.put("carName", car.getName());
        additionalData.put("plateNumber", car.getPlateNumber() != null ? car.getPlateNumber() : "N/A");
        additionalData.put("expirationDate", inspectionDate.toString());
        additionalData.put("daysUntilExpiration", String.valueOf(daysUntilExpiration));
        
        // Check if EXPIRED
        if (inspectionDate.isBefore(today)) {
            String message = String.format(
                "🔧 انتهت صلاحية فحص السيارة %s (لوحة: %s) بتاريخ %s. يرجى إعادة الفحص فوراً.",
                car.getName(),
                car.getPlateNumber() != null ? car.getPlateNumber() : "بدون لوحة",
                inspectionDate
            );
            
            sendNotificationToUsers(car.getCompanyId(), allUserIds, 
                "🔧 انتهاء صلاحية فحص السيارة", 
                message, 
                "INSPECTION_EXPIRED",
                additionalData);
            
            log.info("✅ Sent EXPIRED notification for car {} inspection", car.getId());
            return true;
        }
        
        // Check if EXPIRING WITHIN 7 DAYS
        else if (daysUntilExpiration >= 0 && daysUntilExpiration <= 7) {
            String message = String.format(
                "⚠️ ينتهي فحص السيارة %s (لوحة: %s) خلال %d يوم (تاريخ الانتهاء: %s). يرجى تجديد الفحص.",
                car.getName(),
                car.getPlateNumber() != null ? car.getPlateNumber() : "بدون لوحة",
                daysUntilExpiration,
                inspectionDate
            );
            
            sendNotificationToUsers(car.getCompanyId(), allUserIds, 
                "⚠️ تنبيه: اقتراب انتهاء صلاحية الفحص", 
                message, 
                "INSPECTION_EXPIRING_SOON",
                additionalData);
            
            log.info("✅ Sent EXPIRING SOON ({} days) notification for car {} inspection", 
                     daysUntilExpiration, car.getId());
            return true;
        }
        
        // Check if EXPIRING WITHIN 30 DAYS
        else if (daysUntilExpiration > 7 && daysUntilExpiration <= 30) {
            String message = String.format(
                "📅 سينتهي فحص السيارة %s (لوحة: %s) خلال %d يوم (تاريخ الانتهاء: %s). يرجى التخطيط لإعادة الفحص.",
                car.getName(),
                car.getPlateNumber() != null ? car.getPlateNumber() : "بدون لوحة",
                daysUntilExpiration,
                inspectionDate
            );
            
            sendNotificationToUsers(car.getCompanyId(), allUserIds, 
                "📅 تذكير: فحص السيارة على وشك الانتهاء", 
                message, 
                "INSPECTION_EXPIRING",
                additionalData);
            
            log.info("✅ Sent EXPIRING ({} days) notification for car {} inspection", 
                     daysUntilExpiration, car.getId());
            return true;
        }
        
        log.debug("No notification needed for car {} inspection (expires in {} days)", 
                  car.getId(), daysUntilExpiration);
        return false;
    }
    
    private void sendNotificationToUsers(Long companyId, List<Long> userIds, 
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
            log.debug("Notification sent to {} users in company {}", userIds.size(), companyId);
            
        } catch (Exception e) {
            log.error("Error sending car expiration notification: {}", e.getMessage(), e);
        }
    }
    
    private LocalDateTime getCurrentBaghdadTime() {
        return ZonedDateTime.now(ZoneId.of("Asia/Baghdad")).toLocalDateTime();
    }
}