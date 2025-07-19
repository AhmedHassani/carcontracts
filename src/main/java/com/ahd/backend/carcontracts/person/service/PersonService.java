package com.ahd.backend.carcontracts.person.service;

import com.ahd.backend.carcontracts.S3.S3FileStorageService;
import com.ahd.backend.carcontracts.S3.S3UrlService;
import com.ahd.backend.carcontracts.exception.BadRequestException;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.person.dto.*;
import com.ahd.backend.carcontracts.person.enums.DocSide;
import com.ahd.backend.carcontracts.person.enums.DocType;
import com.ahd.backend.carcontracts.person.mapper.PersonMapper;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.model.PersonAttachment;
import com.ahd.backend.carcontracts.person.repository.PersonAttachmentRepository;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor

@Slf4j
@Transactional
public class PersonService {

    private final PersonRepository personRepository;
    private final S3FileStorageService fileStorageService;
    private final S3UrlService s3UrlService;
    private final PersonAttachmentRepository personAttachmentRepository;

    /**
     * Add person with attachments
     */

    @Transactional
    public PersonResponseDTO addPersonWithAttachments(PersonRequestDTO req) {
        Person person = personRepository.save(PersonMapper.toEntity(req));
        uploadAndAttach(person, req.getNationalIdFrontFile(), DocType.NATIONAL_ID, DocSide.FRONT);
        uploadAndAttach(person, req.getNationalIdBackFile(), DocType.NATIONAL_ID, DocSide.BACK);
        uploadAndAttach(person, req.getResidenceCardFrontFile(), DocType.RESIDENCE_CARD, DocSide.FRONT);
        uploadAndAttach(person, req.getResidenceCardBackFile(), DocType.RESIDENCE_CARD, DocSide.BACK);
        return PersonMapper.toResponse(person);
    }


    /**
     * Get all persons with attachments (paginated)
     */
    @Transactional(readOnly = true)
    public Page<PersonResponseDTO> getAllPersonsWithAttachments(PersonSearchCriteria criteria, Pageable pageable) {
        log.info("Fetching all persons with attachments");
        Specification<Person> spec = new PersonSpecification(criteria);
        Page<Person> persons = personRepository.findAll(spec, pageable);
        return persons.map(PersonMapper::toResponse);
    }

    /**
     * Get person by ID with attachments
     */
    @Transactional(readOnly = true)
    public PersonResponseDTO getPersonById(Long id) {
        log.info("Fetching person by id: {}", id);
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));
        return PersonMapper.toResponse(person);
    }

    /**
     * Replace ONE existing attachment file.
     * <p>Exactly one of the four MultipartFile fields must be present in the request.</p>
     */
    @Transactional
    public PersonAttachmentResponse replaceAttachment(UpdatePersonAttachment dto) {
        if (dto.getFile() == null || dto.getFile().isEmpty())
            throw new BadRequestException("A non-empty file must be supplied");
        PersonAttachment att = personAttachmentRepository
                .findByIdAndPersonId(dto.getAttachmentId(), dto.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Attachment %d not found for person %d".formatted(dto.getAttachmentId(), dto.getId())));
        try { fileStorageService.delete(att.getOriginalName()); }
        catch (Exception ex) { log.warn("Cannot delete old object: {}", ex.getMessage()); }
        String key = fileStorageService.upload(dto.getFile());
        att.setUrl(s3UrlService.getImageUrl(key));
        att.setOriginalName(dto.getFile().getOriginalFilename());
        personAttachmentRepository.save(att);
        return PersonAttachmentResponse.builder()
                .id(att.getId())
                .personId(att.getPerson().getId())
                .docType(att.getDocType())
                .docSide(att.getDocSide())
                .url(att.getUrl())
                .originalName(att.getOriginalName())
                .build();
    }


    @Transactional
    public void deleteAttachmentById(Long attachmentId) {
        PersonAttachment att = personAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attachment " + attachmentId + " not found"));
        Optional.ofNullable(att.getOriginalName()).ifPresent(original -> {
            try { fileStorageService.delete(original); }
            catch (Exception ex) { log.warn("Cannot delete old object: {}", ex.getMessage()); }
        });
        personAttachmentRepository.delete(att);
    }

    @Transactional
    public PersonAttachmentResponse upsertAttachment(Long personId, DocType  type, DocSide  side,MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new BadRequestException("A non-empty file must be supplied");
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person not found: " + personId));
        PersonAttachment att = personAttachmentRepository
                .findByPersonIdAndDocTypeAndDocSide(personId, type, side)
                .orElseGet(() -> PersonAttachment.builder()
                        .person(person)
                        .docType(type)
                        .docSide(side)
                        .build());
        Optional.ofNullable(att.getOriginalName()).ifPresent(original -> {
            try { fileStorageService.delete(original); }
            catch (Exception ex) { log.warn("Cannot delete old object: {}", ex.getMessage()); }
        });
        String key = fileStorageService.upload(file);
        att.setUrl(s3UrlService.getImageUrl(key));
        att.setOriginalName(file.getOriginalFilename());
        personAttachmentRepository.save(att);
        return PersonAttachmentResponse.builder()
                .id(att.getId())
                .personId(personId)
                .docType(type)
                .docSide(side)
                .url(att.getUrl())
                .originalName(att.getOriginalName())
                .build();
    }


    /**
     * Update person information
     */
    public PersonResponseDTO updatePerson(Long id, UpdatePerson personRequest) {
        log.info("Updating person with id: {}", id);
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));
        Person updatedPerson = PersonMapper.merge(personRequest, existingPerson);
        updatedPerson = personRepository.save(updatedPerson);
        return PersonMapper.toResponse(updatedPerson);
    }

    /**
     * Delete person (cascades to attachments)
     */
    public void deletePerson(Long id) {
        log.info("Deleting person with id: {}", id);
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));
        person.getAttachments().forEach(attachment -> {
            try {
                fileStorageService.delete(attachment.getOriginalName());
            } catch (Exception e) {
                log.warn("Failed to delete file from storage: {}", e.getMessage());
            }
        });
        personRepository.delete(person);
    }

    /**
     * Get attachment by ID
     */
    @Transactional(readOnly = true)
    public PersonAttachmentResponse getAttachmentById(Long attachmentId) {
        log.info("Fetching attachment with id: {}", attachmentId);
        PersonAttachment attachment = personAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + attachmentId));
        return PersonAttachmentResponse.builder()
                .id(attachment.getId())
                .personId(attachment.getPerson().getId())
                .docType(attachment.getDocType())
                .docSide(attachment.getDocSide())
                .url(attachment.getUrl())
                .originalName(attachment.getOriginalName())
                .build();
    }

    /**
     * Internal method to add attachments to a person
     */

    private PersonAttachment uploadAndAttach(Person person, MultipartFile file, DocType type, DocSide side) {
        if (file == null || file.isEmpty())
            throw new BadRequestException("Failed to upload customer file to storage.");
        String key = fileStorageService.upload(file);
        String url = s3UrlService.getImageUrl(key);
        PersonAttachment attachment = PersonAttachment.builder()
                .person(person)
                .docType(type)
                .docSide(side)
                .url(url)
                .originalName(file.getOriginalFilename())
                .build();
        return personAttachmentRepository.save(attachment);
    }

}
