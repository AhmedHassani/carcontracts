package com.ahd.backend.carcontracts.person.dto;

import com.ahd.backend.carcontracts.person.enums.DocSide;
import com.ahd.backend.carcontracts.person.enums.DocType;
import lombok.*;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class CreatePersonAttachmentRequest {
    private DocType docType;
    private DocSide docSide;
}