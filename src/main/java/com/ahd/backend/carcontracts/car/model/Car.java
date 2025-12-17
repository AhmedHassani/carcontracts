package com.ahd.backend.carcontracts.car.model;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.company.model.Company;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
//@Table(
//        name = "car",
//        uniqueConstraints = {
//                @UniqueConstraint(
//                        columnNames = {"companyId", "chassis_number"}
//                ) ,
//                @UniqueConstraint(
//                        columnNames = {"companyId" , "plate_number" , "type_of_car_plate" , "wallet_number"}
//                )
//        }
//)

//
//SELECT
//t.name AS TableName,
//c.name AS ConstraintName,
//c.type_desc AS ConstraintType
//FROM sys.objects c
//INNER JOIN sys.tables t ON c.parent_object_id = t.object_id
//WHERE t.name = 'Car';
//
//        -- Drop UNIQUE constraints (if present)
//IF EXISTS (
//        SELECT 1 FROM sys.key_constraints
//                WHERE name = 'UQ_car_companyId_chassis' AND type = 'UQ'
//)
//ALTER TABLE dbo.car DROP CONSTRAINT [UQ_car_companyId_chassis];
//
//IF EXISTS (
//        SELECT 1 FROM sys.key_constraints
//                WHERE name = 'UQ_car_companyId_plate_type_wallet' AND type = 'UQ'
//)
//ALTER TABLE dbo.car DROP CONSTRAINT [UQ_car_companyId_plate_type_wallet];
//
//        -- In case they were created as unique indexes (not constraints), drop those too
//IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_car_companyId_chassis' AND object_id = OBJECT_ID('dbo.car'))
//DROP INDEX [UQ_car_companyId_chassis] ON dbo.car;
//
//IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_car_companyId_plate_type_wallet' AND object_id = OBJECT_ID('dbo.car'))
//DROP INDEX [UQ_car_companyId_plate_type_wallet] ON dbo.car;
//
//
//
//-- See DB default collation (optional)
//        SELECT name, collation_name FROM sys.databases WHERE name = DB_NAME();
//
//-- Align the column to DATABASE_DEFAULT
//ALTER TABLE dbo.car
//ALTER COLUMN [status] VARCHAR(50) COLLATE DATABASE_DEFAULT NOT NULL;
//
//
//CREATE UNIQUE NONCLUSTERED INDEX [UQ_car_companyId_chassis_Pending]
//ON [dbo].[car] ([company_id], [chassis_number])
//WHERE [status] = 'Pending'  AND [deleted] = 0;
//
//CREATE UNIQUE NONCLUSTERED INDEX [UQ_car_company_plate_type_wallet_Pending]
//ON [dbo].[car] ([company_id], [plate_number], [type_of_car_plate], [wallet_number])
//WHERE [status] = 'Pending'  AND [deleted] = 0;




@Where(clause = "deleted = false")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50)
    private String type;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    private String model;

    @Column(name = "plate_number", length = 20)
    private String plateNumber;

    @Column(name = "chassis_number", length = 50)
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

    @OneToMany(mappedBy = "car",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<CarAttachment> attachments = new ArrayList<>();

    @Builder.Default
    private boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "companyId", nullable = false)
    private Long companyId;

    @Column(name = "type_of_car_plate", columnDefinition = "NVARCHAR(20)")
    private String typeOfCarPlate;

    @Column(name = "wallet_number", columnDefinition = "NVARCHAR(20)")
    private String walletNumber;

    private String initPrice;

    private String status;

    private String description;


    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
