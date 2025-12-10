package com.ahd.backend.carcontracts.dropDownList.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionResponseDTO {
    private Long id;
    private String label;
    private String value;
    private Long dropDownId;
}