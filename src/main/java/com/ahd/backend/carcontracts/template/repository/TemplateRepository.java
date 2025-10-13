package com.ahd.backend.carcontracts.template.repository;


import com.ahd.backend.carcontracts.template.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    // List all templates for a company
    List<Template> findByCompanyId(long companyId);

    // Same, but ordered (handy for "latest")
    List<Template> findByCompanyIdOrderByUpdatedAtDesc(long companyId);

    // Guard read/update/delete by company ownership
    Optional<Template> findByIdAndCompanyId(Long id, long companyId);

    // Optional: if you prefer just the newest one
    Optional<Template> findFirstByCompanyIdOrderByUpdatedAtDesc(long companyId);
 }
