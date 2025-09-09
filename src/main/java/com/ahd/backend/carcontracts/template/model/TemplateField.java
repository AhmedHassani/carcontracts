package com.ahd.backend.carcontracts.template.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private String fieldId;
    private double x;
    private double y;
    private double width;
    private double height;
    private String value;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
}
