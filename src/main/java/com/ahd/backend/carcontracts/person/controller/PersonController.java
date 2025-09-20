package com.ahd.backend.carcontracts.person.controller;
import com.ahd.backend.carcontracts.person.dto.*;
import com.ahd.backend.carcontracts.person.enums.DocSide;
import com.ahd.backend.carcontracts.person.enums.DocType;
import com.ahd.backend.carcontracts.person.service.PersonService;
import com.ahd.backend.carcontracts.util.base.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;


@RestController
@RequestMapping("${application.api.base-path}/person")
@RequiredArgsConstructor
@Slf4j
public class PersonController {

    private final PersonService personService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PERSON_CREATE')")
    public ResponseEntity<?> create(@ModelAttribute @Valid PersonRequestDTO person) {
        personService.addPersonWithAttachments(person);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Customer card has been created successfully.")
                .code(200)
                .date(Instant.now())
                .build());
    }


    @GetMapping
    @PreAuthorize("hasAuthority('PERSON_READ')")
    public ResponseEntity<ApiResponse<List<PersonResponseDTO>>> getAllPersons(
            @ModelAttribute PersonSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PersonResponseDTO> personResponse = personService.getAllPersonsWithAttachments(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(personResponse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSON_DELETE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<?>> deletePersons(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Delete Successfully")
                .code(200)
                .date(Instant.now())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSON_READ') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<?>> getPersonByID(@PathVariable Long id) {
        PersonResponseDTO response = personService.getPersonById(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("OK")
                .code(200)
                .data(response)
                .date(Instant.now())
                .build());
    }


    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PERSON_UPDATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PersonAttachmentResponse> replace(
            @Valid @ModelAttribute UpdatePersonAttachment request) {
        PersonAttachmentResponse resp = personService.replaceAttachment(request);
        return ResponseEntity.ok(resp);
    }


    @PutMapping(
            path = "{personId}/attachment/{docType}/{docSide}/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PERSON_UPDATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PersonAttachmentResponse> upsert(
            @PathVariable Long personId,
            @PathVariable DocType docType,
            @PathVariable DocSide docSide,
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {

        PersonAttachmentResponse resp =
                personService.upsertAttachment(personId, docType, docSide, file , id);
        return ResponseEntity.ok(resp);
    }


    @PutMapping(path = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PERSON_UPDATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PersonAttachmentResponse> replaceAttachment(
            @ModelAttribute @Valid UpdatePersonAttachment dto) {
        return ResponseEntity.ok(personService.replaceAttachment(dto));
    }


    @DeleteMapping("attachments/{id}")
    @PreAuthorize("hasAuthority('PERSON_DELETE') or hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteAttachment(@PathVariable Long id) {
        personService.deleteAttachmentById(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Delete Successfully")
                .code(200)
                .date(Instant.now())
                .build());
    }


    @PutMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('PERSON_UPDATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updatePerson(@PathVariable Long id, @Valid @RequestBody UpdatePerson personRequest) {
        PersonResponseDTO dto = personService.updatePerson(id, personRequest);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Person Updated Successfully")
                .code(200)
                .data(dto)
                .date(Instant.now())
                .build());
    }

}


