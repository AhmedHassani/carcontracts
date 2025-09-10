package com.ahd.backend.carcontracts.template.dto;



import lombok.Data;
import java.util.List;

@Data
public class TemplateDTO {
    private Long id;
    private String name;
    private String imageKey;
    private List<TemplateFieldDTO> fields;
}
