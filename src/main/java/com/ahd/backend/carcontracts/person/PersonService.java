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
        return PersonRepository.findAll()
                    .stream().map(person -> PersonResponseDTO.builder()
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
                        .build()
                .collect(Collectors.toList());
    }
}
