package com.ahd.backend.carcontracts.person;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    public PersonResponseDTO createPerson(PersonRequestDTO dto) {
        Person person = Person.builder()
                .username(dto.getUsername())
                .fourth_name(dto.getFourth_name())
                .grandfather_name(dto.getGrandfather_name())
                .father_name(dto.getFather_name())
                .first_name(dto.getFirst_name())
                .nationalId(dto.getNationalId())
                .phone(dto.getPhone())
                .housing_card_number(dto.getHousing_card_number())
                .house_number(dto.getHouse_number())
                .alley(dto.getAlley())
                .neighborhood(dto.getNeighborhood())
                .info_office(dto.getInfo_office())
                .issuing_authority(dto.getIssuing_authority())
                .updatedAt(new Date())
                .build();

        person = personRepository.save(person);

        return PersonResponseDTO.builder()
                .id(person.getId())
                .username(person.getUsername())
                .fourth_name(person.getFourth_name())
                .grandfather_name(person.getGrandfather_name())
                .father_name(person.getFather_name())
                .first_name(person.getFirst_name())
                .nationalId(person.getNationalId())
                .phone(person.getPhone())
                .housing_card_number(person.getHousing_card_number())
                .house_number(person.getHouse_number())
                .alley(person.getAlley())
                .neighborhood(person.getNeighborhood())
                .info_office(person.getInfo_office())
                .issuing_authority(person.getIssuing_authority())
                .createdAt(person.getCreatedAt())
                .build();
    }

    public Page<PersonResponseDTO> getAllPersons(
            String usernameFilter,
            LocalDate createdDateFilter,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Person> personPage = personRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add((Predicate) cb.isFalse(root.get("deleted"))); // Exclude deleted

            if (usernameFilter != null && !usernameFilter.isEmpty()) {
                predicates.add((Predicate) cb.like(cb.lower(root.get("username")), "%" + usernameFilter.toLowerCase() + "%"));
            }

            if (createdDateFilter != null) {
                predicates.add((Predicate) cb.equal(root.get("createdDate").as(LocalDate.class), createdDateFilter));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        return personPage.map(this::convertToResponseDTO);
    }



    public PersonResponseDTO updatePerson(Long id, PersonRequestDTO dto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));

        person.setFirst_name(dto.getFirst_name());
        person.setFather_name(dto.getFather_name());
        person.setPhone(dto.getPhone());
        person.setUsername(dto.getUsername());
        person.setFourth_name(dto.getFourth_name());
        person.setGrandfather_name(dto.getGrandfather_name());
        person.setNationalId(dto.getNationalId());
        person.setHousing_card_number(dto.getHousing_card_number());
        person.setHouse_number(dto.getHouse_number());
        person.setAlley(dto.getAlley());
        person.setNeighborhood(dto.getNeighborhood());
        person.setInfo_office(dto.getInfo_office());
        person.setIssuing_authority(dto.getIssuing_authority());
        person.setUpdatedAt(new Date());

        person = personRepository.save(person);
        return convertToResponseDTO(person);
    }

    private PersonResponseDTO convertToResponseDTO(Person person) {
        return PersonResponseDTO.builder()
                .id(person.getId())
                .username(person.getUsername())
                .fourth_name(person.getFourth_name())
                .grandfather_name(person.getGrandfather_name())
                .father_name(person.getFather_name())
                .first_name(person.getFirst_name())
                .nationalId(person.getNationalId())
                .phone(person.getPhone())
                .housing_card_number(person.getHousing_card_number())
                .house_number(person.getHouse_number())
                .alley(person.getAlley())
                .neighborhood(person.getNeighborhood())
                .info_office(person.getInfo_office())
                .issuing_authority(person.getIssuing_authority())
                .createdAt(person.getCreatedAt())
                .build();
    }

    public void softDeletePerson(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));

        person.setDeleted(true);
        person.setUpdatedAt(new Date());
        personRepository.save(person);
    }

    public PersonResponseDTO getByNationalId(String nationalId) {
        Person person = personRepository
                .findByNationalIdAndDeletedFalse(nationalId)
                .orElseThrow(() -> new RuntimeException("Person not found"));

        return convertToResponseDTO(person);
    }

}
