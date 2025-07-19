package com.ahd.backend.carcontracts.contract.model;

import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.payment.model.PaymentPlan;
import com.ahd.backend.carcontracts.person.model.Person;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "car_contracts")
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
}
