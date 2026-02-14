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


    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TemplateItemKind kind = TemplateItemKind.FIELD;



    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String src;

    @Column(name = "style_font_size")
    private Integer styleFontSize;

    @Column(name = "style_color", length = 32)
    private String styleColor;

    @Column(name = "z_index")
    private Integer zIndex;
    @Column(name = "shape_type")
    private String shapeType;
    @Column(name = "fill_color")
    private String fillColor;
    @Column(name = "stroke_color")
    private String strokeColor;
    @Column(name = "stroke_width")
    private double strokeWidth;

  
    @Column(name = "font_family", length = 2)
    private String fontFamily;

    @Column(name = "fill_opacity")
    @Builder.Default
    private double fillOpacity = 1.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

}


// -- Add font_family column (without COLUMN keyword)
// ALTER TABLE [carcontracts].[dbo].[template_field] 
// ADD font_family NVARCHAR(254) NULL;

// -- Add fill_opacity column (using FLOAT instead of DOUBLE)
// ALTER TABLE [carcontracts].[dbo].[template_field] 
// ADD fill_opacity FLOAT NULL 
// CONSTRAINT DF_template_field_fill_opacity DEFAULT 1.0;
