package com.ahd.backend.carcontracts.company;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    /** رقم الشركة (Primary Key) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long id;
    /** تاريخ الاشتراك */
    @Column(name = "subscription_date", nullable = false)
    private LocalDate subscriptionDate;
    /** تاريخ الانتهاء */
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;
    /** اسم صاحب الشركة */
    @Column(name = "owner_name", length = 150, nullable = false)
    private String ownerName;
    /** رقم صاحب الشركة (مثلاً هاتف أو هوية) */
    @Column(name = "owner_contact", length = 50, nullable = false)
    private String ownerContact;
    /** اسم الشركة */
    @Column(name = "company_name", length = 200, nullable = false)
    private String companyName;
    /** عدد المستخدمين */
    @Column(name = "user_count", nullable = false)
    private Integer userCount;
    /** موقع الشركة (مدينة / عنوان مختصر) */
    @Column(name = "company_location", length = 250)
    private String companyLocation;
    /** الحالة */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CompanyStatus status;
}
