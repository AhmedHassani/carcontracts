package com.ahd.backend.carcontracts.person.dto;

import com.ahd.backend.carcontracts.person.enums.DocSide;
import com.ahd.backend.carcontracts.person.enums.DocType;
import com.ahd.backend.carcontracts.person.model.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonAttachmentResponse {
    private Long id;
    private Long   personId;
    private DocType docType;
    private DocSide docSide;
    private String url;
    private String originalName;
}
