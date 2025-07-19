package com.ahd.backend.carcontracts.person.repository;



import com.ahd.backend.carcontracts.person.enums.DocSide;
import com.ahd.backend.carcontracts.person.enums.DocType;
import com.ahd.backend.carcontracts.person.model.PersonAttachment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface PersonAttachmentRepository
        extends JpaRepository<PersonAttachment, Long> {
    List<PersonAttachment> findByPersonId(Long personId);
    Optional<PersonAttachment> findByIdAndPersonId(Long id, Long personId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)          // ← lock here
    Optional<PersonAttachment> findByPersonIdAndDocTypeAndDocSide(
            Long personId,
            DocType docType,
            DocSide docSide
    );

}
