package com.ahd.backend.carcontracts.dropDownList.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DropDownDTO {
    private Long id;
    private String name;
    private List<OptionDropDownDTO> options;
}