package com.ahd.backend.carcontracts.person.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonResponseDTO {
    private Long   id;
    private String firstName;          // الاسم الاول
    private String fatherName;         // اسم الاب
    private String grandfatherName;    // اسم الجد
    private String fourthName;         // الاسم الرابع
    private String surname;            // اللقب
    private String phoneNumber;        // رقم الهاتف
    private String nationalId;         // رقم الهوية
    private String residenceCardNo;    // رقم بطاقة السكن
    private String residence;          // السكن
    private String district;           // المحلة
    private String alley;              // الزقاق
    private String houseNo;            // الدار
    private String issuingAuthority;   // جهة الاصدار
    private String infoOffice;// مكتب المعلومات
    private List<PersonAttachmentResponse> attachments;
}
