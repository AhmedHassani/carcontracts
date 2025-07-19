package com.ahd.backend.carcontracts.person.model;


import com.ahd.backend.carcontracts.person.enums.DocSide;
import com.ahd.backend.carcontracts.person.enums.DocType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "person_attachment")
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class PersonAttachment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------- owning side ---------- */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    /* ---------- what this file is ---------- */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocType docType;          // NATIONAL_ID or RESIDENCE_CARD

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DocSide docSide;          // FRONT or BACK

    /* ---------- file info ---------- */
    @Column(nullable = false, length = 500)
    private String url;               // S3-signed URL or public key

    @Column(name = "original_name", length = 255)
    private String originalName;
}
