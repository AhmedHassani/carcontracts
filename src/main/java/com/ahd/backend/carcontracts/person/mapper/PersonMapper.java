package com.ahd.backend.carcontracts.person.mapper;

import com.ahd.backend.carcontracts.person.dto.*;
import com.ahd.backend.carcontracts.person.model.*;
import java.util.List;
import java.util.stream.Collectors;

public final class PersonMapper {

    private PersonMapper() {}

    /* ---------- ↓ Entity creation stays the same ↓ ---------- */
    public static Person toEntity(PersonRequestDTO req) {
        if (req == null) return null;
        return Person.builder()
                .firstName       (req.getFirstName())
                .fatherName      (req.getFatherName())
                .grandfatherName (req.getGrandfatherName())
                .fourthName      (req.getFourthName())
                .surname         (req.getSurname())
                .phoneNumber     (req.getPhoneNumber())
                .nationalId      (req.getNationalId())
                .residenceCardNo (req.getResidenceCardNo())
                .residence       (req.getResidence())
                .district        (req.getDistrict())
                .alley           (req.getAlley())
                .houseNo         (req.getHouseNo())
                .issuingAuthority(req.getIssuingAuthority())
                .infoOffice      (req.getInfoOffice())
                .companyId       (req.getCompanyId())
                .build();
    }

    /* ---------- NEW helper for attachment mapping ---------- */
    private static PersonAttachmentResponse toAttachmentResponse(PersonAttachment a) {
        return PersonAttachmentResponse.builder()
                .id        (a.getId())
                .docType   (a.getDocType())
                .docSide   (a.getDocSide())
                .url       (a.getUrl())
                .build();
    }

    /* ---------- Entity → Response (now with attachments) ---------- */
    public static PersonResponseDTO toResponse(Person e) {
        if (e == null) return null;

        List<PersonAttachmentResponse> attachmentDtos =
                e.getAttachments() == null
                        ? List.of()
                        : e.getAttachments().stream()
                        .map(PersonMapper::toAttachmentResponse)
                        .collect(Collectors.toList());

        return PersonResponseDTO.builder()
                .id               (e.getId())
                .firstName        (e.getFirstName())
                .fatherName       (e.getFatherName())
                .grandfatherName  (e.getGrandfatherName())
                .fourthName       (e.getFourthName())
                .surname          (e.getSurname())
                .phoneNumber      (e.getPhoneNumber())
                .nationalId       (e.getNationalId())
                .residenceCardNo  (e.getResidenceCardNo())
                .residence        (e.getResidence())
                .district         (e.getDistrict())
                .alley            (e.getAlley())
                .houseNo          (e.getHouseNo())
                .issuingAuthority (e.getIssuingAuthority())
                .infoOffice       (e.getInfoOffice())
                .attachments      (attachmentDtos)          // ← new line
                .build();
    }

    /* ---------- List mapping ---------- */
    public static List<PersonResponseDTO> toResponseList(List<Person> entities) {
        return entities == null
                ? List.of()
                : entities.stream()
                .map(PersonMapper::toResponse)
                .collect(Collectors.toList());
    }

    /* ---------- Merge (unchanged) ---------- */
    public static Person merge(UpdatePerson req, Person e) {
        e = (e == null) ? new Person() : e;
        if (req == null) return e;

        if (req.getFirstName()       != null) e.setFirstName       (req.getFirstName());
        if (req.getFatherName()      != null) e.setFatherName      (req.getFatherName());
        if (req.getGrandfatherName() != null) e.setGrandfatherName (req.getGrandfatherName());
        if (req.getFourthName()      != null) e.setFourthName      (req.getFourthName());
        if (req.getSurname()         != null) e.setSurname         (req.getSurname());

        if (req.getPhoneNumber()     != null) e.setPhoneNumber     (req.getPhoneNumber());
        if (req.getNationalId()      != null) e.setNationalId      (req.getNationalId());
        if (req.getResidenceCardNo() != null) e.setResidenceCardNo (req.getResidenceCardNo());

        if (req.getResidence()       != null) e.setResidence       (req.getResidence());
        if (req.getDistrict()        != null) e.setDistrict        (req.getDistrict());
        if (req.getAlley()           != null) e.setAlley           (req.getAlley());
        if (req.getHouseNo()         != null) e.setHouseNo         (req.getHouseNo());

        if (req.getIssuingAuthority()!= null) e.setIssuingAuthority(req.getIssuingAuthority());
        if (req.getInfoOffice()      != null) e.setInfoOffice      (req.getInfoOffice());

        return e;
    }
}
