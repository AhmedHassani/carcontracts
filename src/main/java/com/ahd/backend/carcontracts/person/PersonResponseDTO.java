package com.ahd.backend.carcontracts.person;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class PersonResponseDTO {
    private Long id;
    private String username;
    private String fourth_name;
    private String grandfather_name;
    private String father_name;
    private String first_name;
    private String national_id;
    private String phone;
    private String housing_card_number;
    private String house_number;
    private String alley;
    private String neighborhood;
    private String info_office;
    private String issuing_authority;
    private Date createdAt;
}