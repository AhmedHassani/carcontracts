package com.ahd.backend.carcontracts.authorization.model;



import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.person.model.Person;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "authorizations")
public class Authorization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "company_id")
    private Long companyId;

    @NotNull
    @Column(unique = true)
    private Long authorizationNumber;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate authorizationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    Person buyer;

    @NotBlank
    private String companyAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    Car car;

    @Column(name = "template_id")
    private Long templateId;

}
