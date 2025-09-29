package com.ahd.backend.carcontracts.car.repository;

import com.ahd.backend.carcontracts.car.dto.CarSearchCriteria;
import com.ahd.backend.carcontracts.car.model.Car;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {
    Optional<Car> findByIdAndCompanyIdAndDeletedFalse(Long id , Long CompanyId);
    //Optional<Car> findById(Long id);
    boolean existsByChassisNumber(String chassisNumber);
    boolean existsByCompanyId(Long companyId);
    boolean existsByPlateNumber(String plateNumber);
    @EntityGraph(attributePaths = "attachments")
    Optional<Car> findWithAttachmentsByIdAndCompanyId(Long id , Long CompanyId);
    @Query("""
            SELECT COUNT(c)
            FROM Car c
            WHERE c.companyId = :companyId
              AND c.createdAt BETWEEN :start AND :end
            """)
    Long countByCompanyIdAndCreatedAtBetween(@Param("companyId") Long companyId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query("""
            SELECT COUNT(c)
            FROM Car c
            WHERE c.companyId = :companyId
              AND c.status = :status
              AND c.paidAt BETWEEN :start AND :end
            """)
    Long countByCompanyIdAndStatusAndCreatedAtBetween(@Param("companyId") Long companyId,
                                                      @Param("status") String status,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);
}
