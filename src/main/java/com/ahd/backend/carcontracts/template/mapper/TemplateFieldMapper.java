package com.ahd.backend.carcontracts.template.mapper;


import com.ahd.backend.carcontracts.template.dto.TemplateFieldDTO;
import com.ahd.backend.carcontracts.template.model.TemplateField;

public class TemplateFieldMapper {

    public static TemplateFieldDTO toDTO(TemplateField entity) {
        if (entity == null) return null;

        TemplateFieldDTO dto = new TemplateFieldDTO();
        dto.setId(entity.getId());
        dto.setLabel(entity.getLabel());
        dto.setFieldId(entity.getFieldId());
        dto.setX(entity.getX());
        dto.setY(entity.getY());
        dto.setWidth(entity.getWidth());
        dto.setHeight(entity.getHeight());
        dto.setValue(entity.getValue());

        return dto;
    }

    public static TemplateField toEntity(TemplateFieldDTO dto) {
        if (dto == null) return null;

        return TemplateField.builder()
                .id(dto.getId()) // only set if updating
                .label(dto.getLabel())
                .fieldId(dto.getFieldId())
                .x(dto.getX())
                .y(dto.getY())
                .width(dto.getWidth())
                .height(dto.getHeight())
                .value(dto.getValue())
                .build();
    }
}
