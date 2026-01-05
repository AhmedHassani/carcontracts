// src/main/java/com/ahd/backend/carcontracts/template/dto/TemplateFieldDTO.java
package com.ahd.backend.carcontracts.template.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TemplateFieldDTO {
    private Long id;

    private String label;      // FIELD
    private String fieldId;    // FIELD

    private double x;
    private double y;
    private double width;
    private double height;

    private String value;      // TEXT content or (legacy) base64 for IMAGE
    private String kind;       // "TEXT" | "FIELD" | "IMAGE"
    private String src;        // IMAGE preferred source

    private Style style;       // <— nested object to match frontend
    private Integer zIndex;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Style {
        private Integer fontSize;  // use Integer, not int
        private String color;
    }
}
