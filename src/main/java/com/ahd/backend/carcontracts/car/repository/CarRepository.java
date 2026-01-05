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
    boolean existsByChassisNumberAndCompanyIdAndStatus(String chassisNumber , Long compnayId , String status);
    boolean existsByCompanyId(Long companyId);
    boolean existsByPlateNumberAndWalletNumberAndTypeOfCarPlateAndCompanyIdAndStatus(String plateNumber , String walletNumber , String typeOfCarPlate , Long compnayId , String status);
    @EntityGraph(attributePaths = "attachments")
    Optional<Car> findWithAttachmentsByIdAndCompanyId(Long id , Long CompanyId);
    @Query("""
            SELECT COUNT(c)
            FROM Car c
            WHERE c.companyId = :companyId
              AND c.status = :status
              AND c.createdAt BETWEEN :start AND :end
            """)
    Long countByCompanyIdAndCreatedAtBetween(@Param("companyId") Long companyId,
                                             @Param("status") String status,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
    // this issue happen because the paid date
    @Query("""
            SELECT COUNT(c)
            FROM Car c
            WHERE c.companyId = :companyId
              AND (c.status = :status OR c.status = :status2)
              AND c.paidAt BETWEEN :start AND :end
            """)
    Long countByCompanyIdAndStatusAndCreatedAtBetween(@Param("companyId") Long companyId,
                                                      @Param("status") String status,
                                                      @Param("status2") String status2,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);
}
