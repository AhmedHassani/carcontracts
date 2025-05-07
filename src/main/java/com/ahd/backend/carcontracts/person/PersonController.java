package com.ahd.backend.carcontracts.person;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<PersonResponseDTO> create(@RequestBody PersonRequestDTO dto) {
        return ResponseEntity.ok(personService.createPerson(dto));
    }

    @GetMapping
    public ResponseEntity<List<PersonResponseDTO>> getAll() {
        return ResponseEntity.ok(personService.getAllPersons());
    }
}
