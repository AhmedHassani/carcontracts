// src/main/java/com/ahd/backend/carcontracts/template/mapper/TemplateFieldMapper.java
package com.ahd.backend.carcontracts.template.mapper;

import com.ahd.backend.carcontracts.template.dto.TemplateFieldDTO;
import com.ahd.backend.carcontracts.template.model.TemplateField;

public class TemplateFieldMapper {

public static TemplateField toEntity(TemplateFieldDTO d) {
    if (d == null) return null;
    TemplateField e = new TemplateField();
    e.setId(d.getId());
    e.setLabel(d.getLabel());
    e.setFieldId(d.getFieldId());
    e.setX(d.getX());
    e.setY(d.getY());
    e.setWidth(d.getWidth());
    e.setHeight(d.getHeight());
    e.setValue(d.getValue());
    e.setShapeType(d.getShapeType());
    e.setKind(
            d.getKind() == null ? null :
                    com.ahd.backend.carcontracts.template.model.TemplateItemKind.valueOf(d.getKind().toUpperCase())
    );
    e.setSrc(d.getSrc());

    if (d.getStyle() != null) {
        // Always set these common properties
        e.setStyleFontSize(d.getStyle().getFontSize());
        e.setStyleColor(d.getStyle().getColor());
        e.setZIndex(d.getStyle().getZIndex());
        e.setFontFamily(d.getStyle().getFontFamily());
        e.setFillOpacity(d.getStyle().getFillOpacity());
        
        // Always set shape properties (they'll be null for fields, which is fine)
        e.setStrokeColor(d.getStyle().getStrokeColor());
        e.setFillColor(d.getStyle().getFillColor());
        
        e.setStrokeWidth(d.getStyle().getStrokeWidth());
       
    } else {
        e.setStyleFontSize(null);
        e.setStyleColor(null);
        e.setZIndex(0);
        e.setStrokeWidth(0.0);
    }
    return e;
}

public static TemplateFieldDTO toDTO(TemplateField e) {
    if (e == null) return null;
    TemplateFieldDTO d = new TemplateFieldDTO();
    d.setId(e.getId());
    d.setLabel(e.getLabel());
    d.setFieldId(e.getFieldId());
    d.setX(e.getX());
    d.setY(e.getY());
    d.setWidth(e.getWidth());
    d.setHeight(e.getHeight());
    d.setValue(e.getValue());
    d.setKind(e.getKind() == null ? null : e.getKind().name());
    d.setSrc(e.getSrc());
    d.setShapeType(e.getShapeType());
    
    TemplateFieldDTO.Style style = new TemplateFieldDTO.Style();
    boolean hasStyle = false;
    
    if (e.getStyleFontSize() != null) {
        style.setFontSize(e.getStyleFontSize());
        hasStyle = true;
    }
    if (e.getStyleColor() != null) {
        style.setColor(e.getStyleColor());
        hasStyle = true;
    }
    if (e.getZIndex() != null) {
        style.setZIndex(e.getZIndex());
        hasStyle = true;
    }
    if (e.getFontFamily() != null) {
        style.setFontFamily(e.getFontFamily());
        hasStyle = true;
    }
    style.setFillOpacity(e.getFillOpacity());
    hasStyle = true;
    
    if (e.getStrokeColor() != null) {
        style.setStrokeColor(e.getStrokeColor());
        hasStyle = true;
    }
    if (e.getStrokeWidth() != 0) {
        style.setStrokeWidth(e.getStrokeWidth());
        hasStyle = true;
    }
    if (e.getFillColor() != null) {
        style.setFillColor(e.getFillColor());
        hasStyle = true;
    }
    
    if (hasStyle) {
        d.setStyle(style);
    }
    
    return d;
}
}
