package com.ahd.backend.carcontracts.person.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------- Personal names ---------- */
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;           // الاسم الاول

    @Column(name = "father_name", length = 50, nullable = false)
    private String fatherName;          // اسم الاب

    @Column(name = "grandfather_name", length = 50)
    private String grandfatherName;     // اسم الجد

    @Column(name = "fourth_name", length = 50)
    private String fourthName;          // الاسم الرابع

    @Column(name = "surname", length = 50)
    private String surname;             // اللقب

    /* ---------- Contact & IDs ---------- */
    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;         // رقم الهاتف

    @Column(name = "national_id", length = 20, nullable = false)
    private String nationalId;          // رقم الهوية

    @Column(name = "res_card_no", length = 20)
    private String residenceCardNo;     // رقم بطاقة السكن

    /* ---------- Address ---------- */
    @Column(name = "residence", length = 100)
    private String residence;           // السكن

    @Column(name = "district", length = 50)
    private String district;            // المحلة

    @Column(name = "alley", length = 50)
    private String alley;               // الزقاق

    @Column(name = "house_no", length = 20)
    private String houseNo;             // الدار

    /* ---------- Docs ---------- */
    @Column(name = "issuing_authority", length = 100)
    private String issuingAuthority;    // جهة الاصدار

    @Column(name = "info_office", length = 100)
    private String infoOffice;          // مكتب المعلومات

    @OneToMany(mappedBy = "person",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PersonAttachment> attachments = new ArrayList<>();
}
