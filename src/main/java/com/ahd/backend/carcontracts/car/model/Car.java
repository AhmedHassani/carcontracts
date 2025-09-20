package com.ahd.backend.carcontracts.car.model;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.company.model.Company;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "car",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"companyId", "chassis_number", "plate_number"}
                )
        }
)
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false , columnDefinition = "NVARCHAR(50)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String type;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String color;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String model;

    @Column(name = "plate_number" , columnDefinition = "NVARCHAR(50)")
    private String plateNumber;

    @Column(name = "chassis_number" , columnDefinition = "NVARCHAR(50)")
    private String chassisNumber;

    private Integer kilometers;

    @Column(name = "cylinder_count")
    private Integer cylinderCount;

    @Column(name = "passenger_count")
    private Integer passengerCount;

    @Column(name = "engine_type",  columnDefinition = "NVARCHAR(50)")
    private String engineType;

    @Column( columnDefinition = "NVARCHAR(50)")
    private String origin;

    @OneToMany(mappedBy = "car",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<CarAttachment> attachments = new ArrayList<>();

    @Builder.Default
    private boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "companyId", nullable = false)
    private Long companyId;

    @Column(name = "type_of_car_plate", columnDefinition = "NVARCHAR(20)")
    private String typeOfCarPlate;

    @Column(name = "wallet_number", columnDefinition = "NVARCHAR(20)")
    private String walletNumber;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
