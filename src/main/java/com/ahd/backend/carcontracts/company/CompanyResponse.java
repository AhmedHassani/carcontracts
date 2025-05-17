package com.ahd.backend.carcontracts.company;


import com.ahd.backend.carcontracts.util.base.Pagination;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CompanyResponse (

        /** رقم الشركة (PK) */
        Long id,

        /** اسم الشركة */
        String companyName,

        /** اسم صاحب الشركة */
        String ownerName,

        /** رقم صاحب الشركة (هاتف/هوية) */
        String ownerContact,

        /** عدد المستخدمين */
        Integer userCount,

        /** تاريخ الاشتراك */
        LocalDate subscriptionDate,

        /** تاريخ الانتهاء */
        LocalDate expirationDate,

        /** موقع الشركة */
        String companyLocation,

        /** الحالة */
        CompanyStatus status

) {}
