package com.ahd.backend.carcontracts.Person_Attachment;



import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonAttachmentController {

  //  private final PersonService personService;

//    @PostMapping
//    public ResponseEntity<PersonResponseDTO> create(@RequestBody PersonRequestDTO dto) {
//        return ResponseEntity.ok(personService.createPerson(dto));
//    }

    //@GetMapping
  //  public ResponseEntity<List<PersonResponseDTO>> getAll() {
      //  return ResponseEntity.ok(personService.getAllPersons());
  //  }


//    @PostMapping("/person")
//    public ResponseEntity<?> createPerson(
//            @RequestPart("person") PersonRequestDTO dto,
//            @RequestPart("attachments") List<MultipartFile> files,
//            @RequestPart("types") List<String> types
//    ) throws IOException {
//        return ResponseEntity.ok(personService.createPersonWithAttachments(dto, files, types));
//    }

}
