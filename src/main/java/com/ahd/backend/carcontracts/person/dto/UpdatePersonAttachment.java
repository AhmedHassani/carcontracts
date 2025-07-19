package com.ahd.backend.carcontracts.person.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;


@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePersonAttachment {

    @NotNull(message = "person id is required")
    private Long id;               // personId

    @NotNull(message = "attachment id is required")
    private Long attachmentId;

    @NotNull(message = "file is required")
    private MultipartFile file;
}
