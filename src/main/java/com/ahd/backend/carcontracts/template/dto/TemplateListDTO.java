// src/main/java/com/ahd/backend/carcontracts/template/dto/TemplateListDTO.java
package com.ahd.backend.carcontracts.template.dto;

import java.time.Instant;
import lombok.Value;

@Value
public class TemplateListDTO {
    Long id;
    String name;
   // String imageKey;
    Instant createdAt;
    Instant updatedAt;
}
