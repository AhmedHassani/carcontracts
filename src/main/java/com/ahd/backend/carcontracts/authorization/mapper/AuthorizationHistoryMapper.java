package com.ahd.backend.carcontracts.authorization.mapper;

import com.ahd.backend.carcontracts.authorization.dto.AuthorizationHistoryResponse;
import com.ahd.backend.carcontracts.authorization.model.AuthorizationHistory;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.appuser.models.AppUser; // Adjust import based on your User entity

public final class AuthorizationHistoryMapper {

    private AuthorizationHistoryMapper() { }

    // Entity -> Response DTO
    public static AuthorizationHistoryResponse toResponse(AuthorizationHistory e, String userName) {
        if (e == null) return null;
        return AuthorizationHistoryResponse.builder()
                .userName(userName)
                .newBuyerName(getFullName(e.getNewBuyer()))
                .oldBuyerName(getFullName(e.getOldBuyer()))
                .updateDate(e.getUpdateDate())
                .build();
    }

    // Helper method to get full name from Person
    private static String getFullName(Person person) {
        if (person == null) return null;
        
        StringBuilder fullName = new StringBuilder();
        
        if (person.getFirstName() != null && !person.getFirstName().isEmpty()) {
            fullName.append(person.getFirstName());
        }
        if (person.getFatherName() != null && !person.getFatherName().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(person.getFatherName());
        }
        if (person.getGrandfatherName() != null && !person.getGrandfatherName().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(person.getGrandfatherName());
        }
        if (person.getFourthName() != null && !person.getFourthName().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(person.getFourthName());
        }
        if (person.getSurname() != null && !person.getSurname().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(person.getSurname());
        }
        
        return fullName.toString().trim();
    }
    
    // Alternative: Simplified version if you only want first name + surname
    private static String getSimpleFullName(Person person) {
        if (person == null) return null;
        return String.format("%s %s", 
            person.getFirstName() != null ? person.getFirstName() : "",
            person.getSurname() != null ? person.getSurname() : ""
        ).trim();
    }
}