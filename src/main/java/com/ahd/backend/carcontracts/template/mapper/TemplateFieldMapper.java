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

        if (d.getStyle() != null && d.getStyle().getFontSize() != null) {
            e.setStyleFontSize(d.getStyle().getFontSize());
            e.setStyleColor(d.getStyle().getColor());
            e.setZIndex(d.getStyle().getZIndex());
        } else if(d.getStyle() != null && d.getStyle().getStrokeColor() != null) {
            e.setZIndex(d.getStyle().getZIndex());
            e.setStrokeColor(d.getStyle().getStrokeColor());
            e.setStrokeWidth(d.getStyle().getStrokeWidth());
            e.setFillColor(d.getStyle().getFillColor());
        } else {
            e.setStyleFontSize(null);
            e.setStyleColor(null);
            e.setZIndex(0);
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
        // build style object from columns
        if (e.getStyleFontSize() != null || e.getStyleColor() != null) {
            TemplateFieldDTO.Style style = new TemplateFieldDTO.Style();
            style.setFontSize(e.getStyleFontSize());
            style.setColor(e.getStyleColor());
            style.setZIndex(e.getZIndex());
            d.setStyle(style);
        }else if(e.getStrokeColor() != null ){
            TemplateFieldDTO.Style style = new TemplateFieldDTO.Style();
            style.setZIndex(e.getZIndex());
            style.setStrokeColor(e.getStrokeColor());
            style.setStrokeWidth(e.getStrokeWidth());
            style.setFillColor(e.getFillColor());
            d.setStyle(style);
        }
        return d;
    }
}
