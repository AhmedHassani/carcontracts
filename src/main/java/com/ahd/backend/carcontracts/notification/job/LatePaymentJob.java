package com.ahd.backend.carcontracts.notification.job;

import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import com.ahd.backend.carcontracts.payment.repository.InstallmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LatePaymentJob {

    private final InstallmentRepository installmentRepo;
    private final NotificationService notificationService;

    /**
     * Runs daily at 9:00 AM to check for late installments.
     * Logic:
     * 1. Find all unpaid installments that are past due date.
     * 2. Group by Company.
     * 3. Send a single summary notification to the MANAGER of those companies.
     */
    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    @Transactional
    public void checkForLatePayments() {
        log.info("Starting scheduled job: Check Late Payments");
        LocalDate today = LocalDate.now();
        // Find companies with late installments
        // Exclude PAID and CANCELLED
        List<Object[]> results = installmentRepo.countOverdueInstallmentsByCompany(
                today,
                List.of(InstallmentStatus.PAID, InstallmentStatus.CANCELLED));
        for (Object[] result : results) {
            Long companyId = (Long) result[0];
            Long count = (Long) result[1];
            if (count > 0) {
                notificationService.sendNotification(NotificationRequest.builder()
                        .title("تنبيه: دفعات متأخرة")
                        .message("لديك " + count + " دفعة متأخرة تحتاج إلى متابعة.")
                        .actionBy("SYSTEM")
                        .actionType("LATE_PAYMENT_ALERT")
                        .actionDate(LocalDateTime.now())
                        .companyId(companyId)
                        .targetRoles(Collections.singletonList("ROLE_MANAGER")) // Send to Manager
                        .build());
            }
        }
        log.info("Completed scheduled job: Check Late Payments");
    }
}
