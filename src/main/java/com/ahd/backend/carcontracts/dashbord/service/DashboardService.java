package com.ahd.backend.carcontracts.dashbord.service;



import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
import com.ahd.backend.carcontracts.dashbord.enums.DateType;
import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.repository.InstallmentRepository;
import com.ahd.backend.carcontracts.payment.repository.PaymentPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ContractsRepository contractsRepository;
    private final PaymentPlanRepository paymentPlanRepository;
    private final InstallmentRepository installmentRepository;

    public Map<String, Long> getStats(LocalDate start, DateType dateType) {
        LocalDate end;

        switch (dateType) {
            case day:
                end = start.plusDays(1).minusDays(1); // same day
                break;
            case week:
                end = start.plusWeeks(1).minusDays(1);
                break;
            case month:
                end = start.plusMonths(1).minusDays(1);
                break;
            case year:
                end = start.plusYears(1).minusDays(1);
                break;
            default:
                throw new IllegalArgumentException("Unsupported DateType: " + dateType);
        }

        long contractsCount = contractsRepository.countContractsBetweenDates(start, end);
        long completedPlansCount = paymentPlanRepository
                .countByStatusAndDateRange(PaymentStatus.COMPLETED, start.atStartOfDay(), end.atTime(LocalTime.MAX));
        long paidInstallmentsCount = installmentRepository
                .countByStatusAndDateRange(InstallmentStatus.PAID, start, end);

        return Map.of(
                "totalCar", contractsCount,
                "completedCard", completedPlansCount,
                "paidInstallmentsCount", paidInstallmentsCount
        );
    }


}
