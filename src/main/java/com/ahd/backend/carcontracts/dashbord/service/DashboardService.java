package com.ahd.backend.carcontracts.dashbord.service;


import java.util.TreeMap;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.appuser.repository.UserStatsProjection;
import com.ahd.backend.carcontracts.car.repository.CarRepository;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.company.repository.CompanyStatsProjection;
import com.ahd.backend.carcontracts.contract.repository.ContractsRepository;
import com.ahd.backend.carcontracts.dashbord.enums.DateType;
import com.ahd.backend.carcontracts.payment.enums.InstallmentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentStatus;
import com.ahd.backend.carcontracts.payment.enums.PaymentType;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ContractsRepository contractsRepository;
    private final CarRepository carRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PaymentPlanRepository paymentPlanRepository;
    private final InstallmentRepository installmentRepository;
    private final Helper helper;

    public Map<String, Map<String, Long>> getStatsOfUsers() {
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

            UserStatsProjection userCount = userRepository.getUserStats(start, end);

            stats.put(periodName, Map.of(
                    "Active count", userCount.getActiveCount() ,
                    "total count", userCount.getTotalCount()
            ));
        }
        return stats;
    }

    public Map<String, Map<String, Long>> getStatsOfCompany() {
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

            CompanyStatsProjection companyCount = companyRepository.getCompanyStats(start, end);

            stats.put(periodName, Map.of(
                    "Active count", companyCount.getActiveCount() ,
                    "total count", companyCount.getTotalCount()
            ));
        }
        return stats;
    }

    public Map<String, Map<String, Long>> getStats() {
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end;
        LocalDate startPrev;
        LocalDate endPrev;
        //System.out.println("today : " + today);

        Map<String, LocalDate[]> periods = Map.of(
                "day",   new LocalDate[]{ today, today , today.minusDays(1) , today.minusDays(1)  },
                "week",  new LocalDate[]{ today.minusWeeks(1), today , today.minusWeeks(2) , today.minusWeeks(1) },
                "month", new LocalDate[]{ today.minusMonths(1), today , today.minusMonths(2) , today.minusMonths(1)},
                "year",  new LocalDate[]{ today.minusYears(1), today , today.minusYears(2) , today.minusYears(1)}
        );


        Map<String, Map<String, Long>> stats = new LinkedHashMap<>();

        for (Map.Entry<String, LocalDate[]> entry : periods.entrySet()) {
            String periodName = entry.getKey();
            start = entry.getValue()[0];
            end = entry.getValue()[1];
            startPrev = entry.getValue()[2];
            endPrev = entry.getValue()[3];


            long totalCurrentCars = carRepository
                    .countByCompanyIdAndStatusAndCreatedAtBetween( getCompanyId() ,"Paid", "Active" ,start.atStartOfDay(), end.atTime(LocalTime.MAX)  );
            long totalprevCars = carRepository
                    .countByCompanyIdAndStatusAndCreatedAtBetween( getCompanyId() ,"Paid", "Active" ,startPrev.atStartOfDay(), endPrev.atTime(LocalTime.MAX)  );
            // long paidInstallmentsCount = installmentRepository
            //         .countByStatusAndDateRange(InstallmentStatus.PAID, start, end , getCompanyId() );

            BigDecimal paidCashSum = Optional.ofNullable(
                    paymentPlanRepository.summationByStatusAndDateRangeCompleted(
                             start.atStartOfDay(), end.atTime(LocalTime.MAX), getCompanyId()
                    )
            ).orElse(BigDecimal.ZERO);
            // BigDecimal paidInit = Optional.ofNullable(
            //         paymentPlanRepository.summationinitPaymentByDateRangeCompleted(
            //                  PaymentType.INSTALLMENT , start.atStartOfDay(), end.atTime(LocalTime.MAX), getCompanyId()
            //         )
            // ).orElse(BigDecimal.ZERO);
            long paidCashCount = paidCashSum.longValue();
            // long paidInitCount = paidInit.longValue();



            stats.put(periodName, Map.of(
                   "totalCars", totalCurrentCars ,
                  "paidCars", totalprevCars  ,
                   "paidInstallmentsCount", paidCashCount 
            ));
        }
        return stats;
    }

public Map<LocalDate, Long> getInstallmentsDaily(LocalDate start, LocalDate end) {
    Map<LocalDate, Long> result = new LinkedHashMap<>();
    Long companyId = getCompanyId();
    
    // Add intInstallment (down payments) from PaymentPlan
    for (Object[] row : installmentRepository.getIntInstallmentByDay(start, end, companyId)) {
        LocalDate day = (LocalDate) row[0];
        Long amount = ((Number) row[1]).longValue();
        result.merge(day, amount, Long::sum);
    }
    
    // Add installment payments from Installment
    for (Object[] row : installmentRepository.getInstallmentPaymentsByDay(start, end, companyId)) {
        LocalDate day = (LocalDate) row[0];
        Long amount = ((Number) row[1]).longValue();
        result.merge(day, amount, Long::sum);
    }
    
    // Sort by date
    return new TreeMap<>(result);
}

public Map<String, BigDecimal> getInstallmentsMonthly(LocalDate start, LocalDate end) {
    Map<String, BigDecimal> result = new LinkedHashMap<>();
    Long companyId = getCompanyId();
    
    // Add intInstallment (down payments) from PaymentPlan
    for (Object[] row : installmentRepository.getIntInstallmentByMonth(start, end, companyId)) {
        String month = (String) row[0];
        BigDecimal amount = (BigDecimal) row[1];
        result.merge(month, amount, BigDecimal::add);
    }
    
    // Add installment payments from Installment
    for (Object[] row : installmentRepository.getInstallmentPaymentsByMonth(start, end, companyId)) {
        String month = (String) row[0];
        BigDecimal amount = (BigDecimal) row[1];
        result.merge(month, amount, BigDecimal::add);
    }
    
    // Sort by month
    return new TreeMap<>(result);
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
