// src/main/java/com/ahd/backend/carcontracts/template/mapper/TemplateMapper.java
package com.ahd.backend.carcontracts.template.mapper;

import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.template.dto.TemplateDTO;
import com.ahd.backend.carcontracts.template.dto.TemplateFieldDTO;
import com.ahd.backend.carcontracts.template.model.Template;
import com.ahd.backend.carcontracts.template.model.TemplateField;

import java.util.ArrayList;
import java.util.List;

// TemplateMapper.java
public class TemplateMapper {

    public static Template toEntity(TemplateDTO dto, Company company) {
        Template t = Template.builder()
                .id(dto.getId())
                .name(dto.getName())
            //    .imageKey(dto.getImageKey())
                .company(company)
                .updatedAt(dto.getUpdatedAt())
                .createdAt(dto.getCreatedAt())
                .build();

        // ضمان عدم null
        if (t.getFields() == null) {
            t.setFields(new ArrayList<>());
        }

        if (dto.getFields() != null) {
            for (TemplateFieldDTO f : dto.getFields()) {
                TemplateField e = TemplateFieldMapper.toEntity(f);
                e.setTemplate(t);
                t.getFields().add(e);
            }
        }
        return t;
    }

    public static TemplateDTO toDTO(Template entity) {
        List<TemplateFieldDTO> fields = (entity.getFields() == null ? List.<TemplateFieldDTO>of()
                : entity.getFields().stream().map(TemplateFieldMapper::toDTO).toList());

        return TemplateDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
       //         .imageKey(entity.getImageKey())
                .updatedAt(entity.getUpdatedAt())
                .createdAt(entity.getCreatedAt())
                .fields(fields)
                .build();
    }
}

