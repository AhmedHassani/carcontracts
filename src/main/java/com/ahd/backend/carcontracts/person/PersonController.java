package com.ahd.backend.carcontracts.person;


import com.ahd.backend.carcontracts.car.Car;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/carcontracts/v1/person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<PersonResponseDTO> create(@RequestBody PersonRequestDTO dto) {
        return ResponseEntity.ok(personService.createPerson(dto));
    }

    @GetMapping
    public ResponseEntity<Page<PersonResponseDTO>> getAll(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PersonResponseDTO> result = personService.getAllPersons(username, createdDate, page, size);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> updatePerson(
            @PathVariable Long id, @RequestBody PersonRequestDTO dto) {
        return ResponseEntity.ok(personService.updatePerson(id, dto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        personService.softDeletePerson(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{nationalId}")
    public ResponseEntity<PersonResponseDTO> getByNationalId(
            @PathVariable String nationalId) {
        return ResponseEntity.ok(personService.getByNationalId(nationalId));
    }
    @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Person> updatePersonPhoto(
            @PathVariable Long id,
            @RequestPart("photo") MultipartFile photo) {

        Person updatedPerson = personService.updatePersonPhoto(photo, id);

        return ResponseEntity.ok(updatedPerson);
    }
}
