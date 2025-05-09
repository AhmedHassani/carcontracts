package com.ahd.backend.carcontracts.Person_Attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonAttachmentService {

    private final PersonAttachmentRepository PersonAttachmentRepository;

//    public PersonResponseDTO createPerson(PersonRequestDTO dto) {
//        Person person = Person.builder()
//                .username(dto.getUsername())
//                .fourth_name(dto.getFourth_name())
//                .grandfather_name(dto.getGrandfather_name())
//                .father_name(dto.getFather_name())
//                .first_name(dto.getFirst_name())
//                .national_id(dto.getNational_id())
//                .phone(dto.getPhone())
//                .housing_card_number(dto.getHousing_card_number())
//                .house_number(dto.getHouse_number())
//                .alley(dto.getAlley())
//                .neighborhood(dto.getNeighborhood())
//                .info_office(dto.getInfo_office())
//                .issuing_authority(dto.getIssuing_authority())
//                .build();
//
//        person = personRepository.save(person);
//
//        return PersonResponseDTO.builder()
//                .id(person.getId())
//                .username(person.getUsername())
//                .fourth_name(person.getFourth_name())
//                .grandfather_name(person.getGrandfather_name())
//                .father_name(person.getFather_name())
//                .first_name(person.getFirst_name())
//                .national_id(person.getNational_id())
//                .phone(person.getPhone())
//                .housing_card_number(person.getHousing_card_number())
//                .house_number(person.getHouse_number())
//                .alley(person.getAlley())
//                .neighborhood(person.getNeighborhood())
//                .info_office(person.getInfo_office())
//                .issuing_authority(person.getIssuing_authority())
//                .createdAt(person.getCreatedAt())
//                .build();
//    }
//
//    public List<PersonResponseDTO> getAllPersons() {
//        return personRepository.findAll().stream()
//                .map(person -> PersonResponseDTO.builder()
//                        .id(person.getId())
//                        .username(person.getUsername())
//                        .fourth_name(person.getFourth_name())
//                        .grandfather_name(person.getGrandfather_name())
//                        .father_name(person.getFather_name())
//                        .first_name(person.getFirst_name())
//                        .national_id(person.getNational_id())
//                        .phone(person.getPhone())
//                        .housing_card_number(person.getHousing_card_number())
//                        .house_number(person.getHouse_number())
//                        .alley(person.getAlley())
//                        .neighborhood(person.getNeighborhood())
//                        .info_office(person.getInfo_office())
//                        .issuing_authority(person.getIssuing_authority())
//                        .createdAt(person.getCreatedAt())
//                        .build())
//                .collect(Collectors.toList());
//    }

//    public PersonResponseDTO createPerson(
//            PersonRequestDTO dto,
//            List<MultipartFile> files,
//            List<String> types
//    ) throws IOException {
//
//        Person person = Person.builder()
//                .username(dto.getUsername())
//                .fourth_name(dto.getFourth_name())
//                .grandfather_name(dto.getGrandfather_name())
//                .father_name(dto.getFather_name())
//                .first_name(dto.getFirst_name())
//                .national_id(dto.getNational_id())
//                .phone(dto.getPhone())
//                .housing_card_number(dto.getHousing_card_number())
//                .house_number(dto.getHouse_number())
//                .alley(dto.getAlley())
//                .neighborhood(dto.getNeighborhood())
//                .info_office(dto.getInfo_office())
//                .issuing_authority(dto.getIssuing_authority())
//                .build();
//
//        person = personRepository.save(person);
//
//        String uploadDir = "uploads/person_" + person.getId();
//        Files.createDirectories(Paths.get(uploadDir));
//
//        for (int i = 0; i < files.size(); i++) {
//            MultipartFile file = files.get(i);
//            String type = types.get(i);
//
//            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            Path filePath = Paths.get(uploadDir, filename);
//            Files.write(filePath, file.getBytes());
//
//            Attachment attachment = new Attachment();
//            attachment.setPerson(person);
//            attachment.setFilePath(filePath.toString());
//            attachment.setType(type); // add this to your entity
//            attachmentRepository.save(attachment);
//        }
//
//        return PersonResponseDTO.builder()
//                .id(person.getId())
//                .username(person.getUsername())
//                .build();
//    }

}
