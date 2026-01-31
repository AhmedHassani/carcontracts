// src/main/java/com/ahd/backend/carcontracts/template/dto/TemplateFieldDTO.java
package com.ahd.backend.carcontracts.template.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TemplateFieldDTO {
    private Long id;

    private String label;
    private String fieldId;

    private double x;
    private double y;
    private double width;
    private double height;
    private String shapeType;

    private String value;
    private String kind;
    private String src;

    private Style style;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Style {

        private Integer fontSize;
        private String color;

        private Integer zIndex;

        private double strokeWidth;
        private String fillColor;
        private String strokeColor;

        @JsonProperty("zIndex")
        public Integer getZIndex() {
            return zIndex;
        }
    }
}