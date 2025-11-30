package com.ahd.backend.carcontracts.contract.model;

import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import com.ahd.backend.carcontracts.person.model.Person;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
@Entity
@Data
@Table(name = "car_contracts")
@SQLDelete(sql = "UPDATE car_contracts SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Contracts {
    @Id
    @GeneratedValue
    Long id;

    LocalDate contractDate;

    @ManyToOne
    Person seller;

    @ManyToOne
    Person buyer;

    @ManyToOne
    Person guarantor;

    @ManyToOne
    Car car;

    @ManyToOne
    PaymentPlan paymentPlan;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "template_id")
    private Long templateId;
}
