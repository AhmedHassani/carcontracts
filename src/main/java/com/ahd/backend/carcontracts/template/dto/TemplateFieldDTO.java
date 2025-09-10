package com.ahd.backend.carcontracts.template.dto;


import lombok.Data;

@Data
public class TemplateFieldDTO {
    private Long id;
    private String label;
    private String fieldId;
    private double x;
    private double y;
    private double width;
    private double height;
    private String value;
}

