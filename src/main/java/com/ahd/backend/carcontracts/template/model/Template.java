package com.ahd.backend.carcontracts.template.model;

import com.ahd.backend.carcontracts.company.model.Company;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "template", indexes = { @Index(name = "ix_template_company_id", columnList = "company_id") })
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // name عندك NVARCHAR فعادي تتركها
    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    // مهم: اسم العمود camelCase في DB
    @Column(name = "imageKey", length = 1024)
    private String imageKey;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("zIndex ASC, id ASC")
    @Builder.Default
    private List<TemplateField> fields = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // مهم: اسم العمود camelCase في DB
    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private Instant createdAt;

    // مهم: اسم العمود camelCase في DB
    @UpdateTimestamp
    @Column(name = "updatedAt")
    private Instant updatedAt;
}
