package com.ahd.backend.carcontracts.Car;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String model;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    private String type;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(name = "plate_number", length = 20, unique = true)
    private String plateNumber;

    @Column(name = "chassis_number", length = 50, unique = true)
    private String chassisNumber;

    private Integer kilometers;

    @Column(name = "cylinder_count")
    private Integer cylinderCount;

    @Column(name = "passenger_count")
    private Integer passengerCount;

    @Column(name = "engine_type", length = 50)
    private String engineType;

    @Column(length = 50)
    private String origin;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
