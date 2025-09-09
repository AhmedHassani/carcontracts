package com.ahd.backend.carcontracts.template.repository;


import com.ahd.backend.carcontracts.template.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    // Find all templates by company
    List<Template> findByCompanyId(Long companyId);
    // Optional: find by name inside a company
    List<Template> findByCompanyIdAndNameContaining(Long companyId, String name);
}
