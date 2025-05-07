package com.ahd.backend.carcontracts.person;

import com.ahd.backend.carcontracts.branch.BranchResponseDTO;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class PersonService {

    private final PersonRepository PersonRepository;

    public PersonService(PersonRepository personRepository) {
        PersonRepository = personRepository;
    }

    public PersonResponseDTO createPerson(PersonRequestDTO dto) {

        Person person = Person.builder()
                .username(getUsername())
                .fourth_name(getFourth_name())
                .grandfather_name(getGrandfather_name())
                .father_name(getFather_name())
                .first_name(getFirst_name())
                .national_id(getNational_id())
                .phone(getPhone())
                .housing_card_number(getHousing_card_number())
                .house_number(getHouse_number())
                .alley(getAlley())
                .neighborhood(getNeighborhood())
                .info_office(getInfo_office())
                .issuing_authority(getIssuing_authority())
                .updatedAt(new Date())
                .build();

        person = personRepository.save(person);

        return PersonResponseDTO.builder()
                .id(branch.getId())
                .username(getUsername())
                .fourth_name(getFourth_name())
                .grandfather_name(getGrandfather_name())
                .father_name(getFather_name())
                .first_name(getFirst_name())
                .national_id(getNational_id())
                .phone(getPhone())
                .housing_card_number(getHousing_card_number())
                .house_number(getHouse_number())
                .alley(getAlley())
                .neighborhood(getNeighborhood())
                .info_office(getInfo_office())
                .issuing_authority(getIssuing_authority())
                .createdAt(getCreatedAt())
                .build();
    }
    public List<PersonResponseDTO> getAllPersons() {
        return personRepository.findAll()
                .stream()
                .filter(person -> !person.isDeleted())
                .map(person -> PersonResponseDTO.builder()
                        .id(person.getId())
                        .username(person.getUsername())
                        .fourth_name(person.getFourth_name())
                        .grandfather_name(person.getGrandfather_name())
                        .father_name(person.getFather_name())
                        .first_name(person.getFirst_name())
                        .national_id(person.getNational_id())
                        .phone(person.getPhone())
                        .housing_card_number(person.getHousing_card_number())
                        .house_number(person.getHouse_number())
                        .alley(person.getAlley())
                        .neighborhood(person.getNeighborhood())
                        .info_office(person.getInfo_office())
                        .issuing_authority(person.getIssuing_authority())
                        .createdAt(person.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
    public void softDeletePerson(Long id) {
        Person person = personRepository.findById(id);

        person.setDeleted(true);
        person.setUpdatedAt(new Date());
        personRepository.save(person);
    }
    public PersonResponseDTO updatePerson(Long id, PersonRequestDTO dto) {
        Person person = personRepository.findById(id);

        person.setFirst_name(dto.getFirst_name());
        person.setFather_name(dto.getFather_name());
        person.setPhone(dto.getPhone());
        person.SetUsername(dto.getUsername());
        person.SetFourth_name(dto.getFourth_name());
        person.SetGrandfather_name(dto.getGrandfather_name());
        person.SetNational_id(dto.getNational_id());
        person.SetHousing_card_number(dto.getHousing_card_number());
        person.SetHouse_number(dto.getHouse_number());
        person.SetAlley(dto.getAlley());
        person.SetNeighborhood(dto.getNeighborhood());
        person.SetInfo_office(dto.getInfo_office());
        person.SetIssuing_authority(dto.getIssuing_authority());
        person.setUpdatedAt(new Date());

        person = personRepository.save(person);
        return convertToResponseDTO(person);
    }


}
