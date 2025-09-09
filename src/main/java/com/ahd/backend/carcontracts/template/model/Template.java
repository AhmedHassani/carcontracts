package com.ahd.backend.carcontracts.template.model;


import com.ahd.backend.carcontracts.company.model.Company;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String imageKey;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemplateField> fields;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

}
