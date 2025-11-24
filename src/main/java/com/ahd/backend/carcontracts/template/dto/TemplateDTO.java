package com.ahd.backend.carcontracts.template.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TemplateDTO {
    private Long id;
    private String name;
  //  private String imageKey;
    private List<TemplateFieldDTO> fields;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
