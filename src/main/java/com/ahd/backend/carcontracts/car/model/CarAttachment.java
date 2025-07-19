package com.ahd.backend.carcontracts.car.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "car_attachment")
@Builder
public class CarAttachment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning side */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(name = "file_key", nullable = false, length = 255)
    private String fileKey;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;
}
