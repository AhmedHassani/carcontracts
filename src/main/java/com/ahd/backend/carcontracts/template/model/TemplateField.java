// src/main/java/com/ahd/backend/carcontracts/template/model/TemplateField.java
package com.ahd.backend.carcontracts.template.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "template_field",
        indexes = {
                @Index(name = "ix_template_field_template_id", columnList = "template_id"),
                @Index(name = "ix_template_field_field_id", columnList = "fieldId")
        })
public class TemplateField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String label;

    @Column(length = 255)
    private String fieldId;

    @Column(nullable = false)
    private double x;

    @Column(nullable = false)
    private double y;

    @Column(nullable = false)
    private double width;

    @Column(nullable = false)
    private double height;


    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TemplateItemKind kind = TemplateItemKind.FIELD;


    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String src;

    @Column(name = "style_font_size")
    private Integer styleFontSize;

    @Column(name = "style_color", length = 32)
    private String styleColor;

    @Column(name = "z_index")
    private Integer zIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
}
