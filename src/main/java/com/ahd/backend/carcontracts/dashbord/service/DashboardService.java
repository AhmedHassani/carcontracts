package com.ahd.backend.carcontracts.dashbord.service;



import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
import com.ahd.backend.carcontracts.dashbord.enums.DateType;
import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.repository.InstallmentRepository;
import com.ahd.backend.carcontracts.payment.repository.PaymentPlanRepository;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final Helper helper;


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

            long contractsCount = contractsRepository.countContractsBetweenDates(start, end , getCompanyId());
            long completedPlansCount = paymentPlanRepository
                    .countByStatusAndDateRange(PaymentStatus.COMPLETED, start.atStartOfDay(), end.atTime(LocalTime.MAX) , getCompanyId());
            long paidInstallmentsCount = installmentRepository
                    .countByStatusAndDateRange(InstallmentStatus.PAID, start, end , getCompanyId() );

            stats.put(periodName, Map.of(
                   "contractsCount", contractsCount ,
                  "completedPlansCount", completedPlansCount  ,
                  "paidInstallmentsCount", paidInstallmentsCount
            ));
        }

        return stats;

    }

    public Map<LocalDate, Long> getInstallmentsDaily( LocalDate start, LocalDate end) {
        Map<LocalDate, Long> result = new LinkedHashMap<>();
        for (Object[] row : installmentRepository.countByDay( start, end , getCompanyId())) {
            result.put((LocalDate) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    public Map<String, BigDecimal> getInstallmentsMonthly(LocalDate start, LocalDate end) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Object[] row : installmentRepository.countByMonth(start, end , getCompanyId())) {
            String month = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            result.put(month, total);
        }
        return result;
    }

    public Map<LocalDate, Long> getContractDaily( LocalDate start, LocalDate end) {
        Map<LocalDate, Long> result = new LinkedHashMap<>();
        for (Object[] row : contractsRepository.countByDay( start, end , getCompanyId())) {
            result.put((LocalDate) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    public Map<String, Long> getContractMonthly(LocalDate start, LocalDate end) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : contractsRepository.countByMonth(start, end , getCompanyId())) {
            result.put((String) row[0], ((Number) row[1]).longValue());
        }
        return result;
    }

    public Long getCompanyId (){
        return  helper.getCurrentCompanyId();
    }

}
