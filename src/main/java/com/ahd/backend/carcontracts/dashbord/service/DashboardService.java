package com.ahd.backend.carcontracts.dashbord.service;



import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
import com.ahd.backend.carcontracts.dashbord.enums.DateType;
import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.repository.InstallmentRepository;
import com.ahd.backend.carcontracts.payment.repository.PaymentPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ContractsRepository contractsRepository;
    private final PaymentPlanRepository paymentPlanRepository;
    private final InstallmentRepository installmentRepository;

    public Map<String, Map<String, Long>> getStats() {
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end;

        Map<String, LocalDate[]> periods = Map.of(
                "day",   new LocalDate[]{ today, today },
                "week",  new LocalDate[]{ today.with(DayOfWeek.MONDAY), today.with(DayOfWeek.SUNDAY) },
                "month", new LocalDate[]{ today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()) },
                "year",  new LocalDate[]{ today.withDayOfYear(1), today.withDayOfYear(today.lengthOfYear()) }
        );

        Map<String, Map<String, Long>> stats = new LinkedHashMap<>();

        for (Map.Entry<String, LocalDate[]> entry : periods.entrySet()) {
            String periodName = entry.getKey();
            start = entry.getValue()[0];
            end = entry.getValue()[1];

            long contractsCount = contractsRepository.countContractsBetweenDates(start, end);  // LocalDate
            long completedPlansCount = paymentPlanRepository
                    .countByStatusAndDateRange(PaymentStatus.COMPLETED, start.atStartOfDay(), end.atTime(LocalTime.MAX));  // LocalDateTime
            long paidInstallmentsCount = installmentRepository
                    .countByStatusAndDateRange(InstallmentStatus.PAID, start, end);  // LocalDate

            stats.put(periodName, Map.of(
                   "contractsCount", contractsCount ,
                  "completedPlansCount", completedPlansCount  ,
                  "paidInstallmentsCount", paidInstallmentsCount
            ));
        }

        return stats;

    }
    public Map<Integer, Long> getInstallmentsHourly(LocalDate start, LocalDate end) {
        Map<Integer, Long> result = new LinkedHashMap<>();

        LocalDateTime startDateTime = start.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX); // 23:59:59.999999999

        for (Object[] row : installmentRepository.countHourly(startDateTime, endDateTime)) {
            result.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return result;
    }



    public Map<LocalDate, Long> getInstallmentsDaily( LocalDate start, LocalDate end) {
        Map<LocalDate, Long> result = new LinkedHashMap<>();
        for (Object[] row : installmentRepository.countByDay( start, end)) {
            result.put((LocalDate) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    public Map<Integer, Long> getInstallmentsMonthly( LocalDate start, LocalDate end) {
        Map<Integer, Long> result = new LinkedHashMap<>();
        for (Object[] row : installmentRepository.countByMonth( start, end)) {
            result.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return result;
    }

    public Map<LocalDate, Long> getContractDaily( LocalDate start, LocalDate end) {
        Map<LocalDate, Long> result = new LinkedHashMap<>();
        for (Object[] row : contractsRepository.countByDay( start, end)) {
            result.put((LocalDate) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    public Map<Integer, Long> getContractMonthly( LocalDate start, LocalDate end) {
        Map<Integer, Long> result = new LinkedHashMap<>();
        for (Object[] row : contractsRepository.countByMonth( start, end)) {
            result.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return result;
    }


}
