package com.ahd.backend.carcontracts.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

public record CompanyRequest(

        /** تاريخ الاشتراك */
        @NotNull
        LocalDate subscriptionDate,

        /** تاريخ الانتهاء */
        @NotNull
        LocalDate expirationDate,

        /** اسم صاحب الشركة */
        @NotBlank
        @Size(max = 150)
        String ownerName,

        /** رقم صاحب الشركة (هاتف أو هوية) */
        @NotBlank
        @Size(max = 50)
        String ownerContact,

        /** اسم الشركة */
        @NotBlank
        @Size(max = 200)
        String companyName,

        /** عدد المستخدمين */
        @NotNull
        @Positive
        Integer userCount,

        /** موقع الشركة (اختياري) */
        @Size(max = 250)
        String companyLocation,

        /** الحالة */
        @NotNull
        CompanyStatus status
) {}
