package com.ahd.backend.carcontracts.person.service;

import com.ahd.backend.carcontracts.S3.S3FileStorageService;
import com.ahd.backend.carcontracts.S3.S3UrlService;
import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.car.dto.CarSearchCriteria;

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
import com.ahd.backend.carcontracts.util.Helper;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ahd.backend.carcontracts.notification.service.NotificationService;
import com.ahd.backend.carcontracts.notification.dto.NotificationRequest;

import java.util.Arrays;
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
    private final Helper helper;
    private final NotificationService notificationService ;
    /**
     * Add person with attachments
     */

    @Transactional
    @Auditable(operation = "اضافة مستخدم جديد", captureArgs = true, captureResult = true)
    public PersonResponseDTO addPersonWithAttachments(PersonRequestDTO req) {
        req.setCompanyId(getCompanyId());
        Person person = personRepository.save(PersonMapper.toEntity(req));
        uploadAndAttach(person, req.getNationalIdFrontFile(), DocType.NATIONAL_ID, DocSide.FRONT);
        uploadAndAttach(person, req.getNationalIdBackFile(), DocType.NATIONAL_ID, DocSide.BACK);
        uploadAndAttach(person, req.getResidenceCardFrontFile(), DocType.RESIDENCE_CARD, DocSide.FRONT);
        uploadAndAttach(person, req.getResidenceCardBackFile(), DocType.RESIDENCE_CARD, DocSide.BACK);
        if (req.getOthreFiles() != null) {
            for (MultipartFile file : req.getOthreFiles()) {
                if (file != null && !file.isEmpty()) {
                    uploadAndAttach(person, file, DocType.OTHER_FILE , DocSide.OTHER);
                }
            }
        }
    var currentUser = helper.getCurrentUser();
    
    NotificationRequest notification = new NotificationRequest();
    notification.setTitle("اضافة مستخدم");
    notification.setMessage(req.getFirstName() + " تم اضافة المستخدم");
    notification.setActionBy(currentUser.getUsername());
    notification.setActionType("اضافة مستخدم");
    notification.setCompanyId(getCompanyId());
    notification.setTargetUserIds(Arrays.asList(currentUser.getId()));
    
    notificationService.sendNotification(notification);
            
        
        
        return PersonMapper.toResponse(person);
    }


    @Transactional(readOnly = true)
    public Page<PersonResponseDTO> getAllPersonsWithAttachments(PersonSearchCriteria criteria, Pageable pageable) {
        Specification<Person> spec = PersonSpecification.buildSpecification(criteria);
        PersonSearchCriteria enrichedCriteria = PersonSearchCriteria.builder()
                .keyword(criteria.keyword())
                .sortBy(criteria.sortBy())
                .sortDirection(criteria.sortDirection())
                .phoneNumber(criteria.phoneNumber())
                .nationalId(criteria.nationalId())
                .residenceCardNo(criteria.residenceCardNo())
                .residence(criteria.residence())
                .companyId(getCompanyId())
                .build();
        spec = PersonSpecification.buildSpecification(enrichedCriteria);
        System.out.println("the company id : " + getCompanyId());
        Page<Person> persons = personRepository.findAll(spec, pageable);
        return persons.map(PersonMapper::toResponse);
    }


    @Transactional(readOnly = true)
    public PersonResponseDTO getPersonById(Long id) {
        log.info("Fetching person by id: {}", id);
        Person person = personRepository.findByIdAndCompanyId(id , getCompanyId())
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));
        return PersonMapper.toResponse(person);
    }
    //notification
    //تم تغير الصورة الشخصيه
    //only for current user
    @Auditable(operation = "تغير صورة مستخدم", captureArgs = true, captureResult = true)
    @Transactional
    public PersonAttachmentResponse replaceAttachment(UpdatePersonAttachment dto) {
        if (dto.getFile() == null || dto.getFile().isEmpty())
            throw new BadRequestException("A non-empty file must be supplied");


        PersonAttachment att = personAttachmentRepository
                .findByIdAndPersonId(dto.getAttachmentId(), dto.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Attachment %d not found for person %d".formatted(dto.getAttachmentId(), dto.getId())));
        Person testAuth = personRepository.findByIdAndCompanyId(att.getPerson().getId() , getCompanyId())
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + att.getPerson().getId()));
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

    //notification
    //تم حذف الصورةالشخصية
    //only for current user
    @Transactional
    @Auditable(operation = "حذف صورة مستخدم", captureArgs = true, captureResult = true)
    public void deleteAttachmentById(Long attachmentId) {

        PersonAttachment att = personAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attachment " + attachmentId + " not found"));
        Person testAuth = personRepository.findByIdAndCompanyId(att.getPerson().getId() , getCompanyId())
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + att.getPerson().getId()));
        Optional.ofNullable(att.getOriginalName()).ifPresent(original -> {
            try { fileStorageService.delete(original); }
            catch (Exception ex) { log.warn("Cannot delete old object: {}", ex.getMessage()); }
        });
        personAttachmentRepository.delete(att);
    }
    //notification
    //تم اضافة الصورةالشخصية
    //only for current user
    @Transactional
    @Auditable(operation = "اضافة صورة مستخدم", captureArgs = true, captureResult = true)
    public PersonAttachmentResponse upsertAttachment(Long personId, DocType  type, DocSide  side,MultipartFile file ,long id) {
        if (file == null || file.isEmpty())
            throw new BadRequestException("A non-empty file must be supplied");
        Person person = personRepository.findByIdAndCompanyId(personId , getCompanyId())
                .orElseThrow(() -> new RuntimeException("Person not found: " + personId));
        PersonAttachment att = personAttachmentRepository
                .findByPersonIdAndDocTypeAndDocSideAndId(personId, type, side ,id)
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


    //notification
    //تم تحديث المعلومات
    //only for current user
    @Auditable(operation = "تحديث معلومات مستخدم", captureArgs = true, captureResult = true)
    public PersonResponseDTO updatePerson(Long id, UpdatePerson personRequest) {
        log.info("Updating person with id: {}", id);
        Person existingPerson = personRepository.findByIdAndCompanyId(id ,  getCompanyId())
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));
        Person updatedPerson = PersonMapper.merge(personRequest, existingPerson);
        updatedPerson = personRepository.save(updatedPerson);
        return PersonMapper.toResponse(updatedPerson);
    }

    /**
     * Delete person (cascades to attachments)
     */
    //notification
    //تم حذف المستخدم person.firstName +" "+ person.fatherName +" "+ person.grandfatherName
    //only for current user
    public void deletePerson(Long id) {
       // log.info("Deleting person with id: {}", id);
        Person person = personRepository.findByIdAndCompanyId(id , getCompanyId())
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
        Person testAuth = personRepository.findByIdAndCompanyId(attachment.getPerson().getId() , getCompanyId())
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + attachment.getPerson().getId()));
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
        Person testAuth = personRepository.findByIdAndCompanyId(person.getId() , getCompanyId())
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + person.getId()));
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
    public Long getCompanyId (){
        return  helper.getCurrentCompanyId();
    }
}
