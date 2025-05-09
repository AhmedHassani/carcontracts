package com.ahd.backend.carcontracts.Person_Attachment;

import com.ahd.backend.carcontracts.person.Person;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@Builder
public class PersonAttachmentResponseDTO {

    private Long id;
    private String filePath;
    private String type;
    private Person person;
    private Date createdAt;
}