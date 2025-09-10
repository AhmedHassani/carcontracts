package com.ahd.backend.carcontracts.template.mapper;



import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.template.dto.TemplateDTO;
import com.ahd.backend.carcontracts.template.model.Template;

import java.util.stream.Collectors;

public class TemplateMapper {

    public static TemplateDTO toDTO(Template entity) {
        if (entity == null) return null;
        TemplateDTO dto = new TemplateDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setImageKey(entity.getImageKey());
        if (entity.getFields() != null) {
            dto.setFields(
                    entity.getFields().stream()
                            .map(TemplateFieldMapper::toDTO)
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }

    public static Template toEntity(TemplateDTO dto, Company company) {
        if (dto == null) return null;
        Template template = Template.builder()
                .id(dto.getId())
                .name(dto.getName())
                .imageKey(dto.getImageKey())
                .company(company)
                .build();
        if (dto.getFields() != null) {
            var fields = dto.getFields().stream()
                    .map(TemplateFieldMapper::toEntity)
                    .collect(Collectors.toList());
            fields.forEach(f -> f.setTemplate(template));
            template.setFields(fields);
        }
        return template;
    }
}
