package com.ahd.backend.carcontracts.authorization.service;

import com.ahd.backend.carcontracts.authorization.model.Authorization;
import com.ahd.backend.carcontracts.authorization.model.AuthorizationHistory;
import com.ahd.backend.carcontracts.authorization.repository.AuthorizationHistoryRepository;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorizationHistoryService {

    private final AuthorizationHistoryRepository historyRepository;
    private final PersonRepository personRepository;

    private Integer getNextChangeNumber(Long authorizationId) {
        Integer maxChangeNumber = historyRepository.findMaxChangeNumberByAuthorizationId(authorizationId);
        return (maxChangeNumber == null) ? 1 : maxChangeNumber + 1;
    }

    // This method matches the call from AuthorizationService (4 parameters)
    public void createHistoryEntry(Authorization authorization, Long newBuyerId, Long oldBuyerId, Long userId) {
        Person newBuyer = null;
        Person oldBuyer = null;
        
        if (newBuyerId != null) {
            newBuyer = personRepository.findById(newBuyerId)
                    .orElseThrow(() -> new EntityNotFoundException("New buyer not found: " + newBuyerId));
        }
        
        if (oldBuyerId != null) {
            oldBuyer = personRepository.findById(oldBuyerId)
                    .orElseThrow(() -> new EntityNotFoundException("Old buyer not found: " + oldBuyerId));
        }
        
        Integer changeNumber = getNextChangeNumber(authorization.getId());
        
        AuthorizationHistory history = AuthorizationHistory.builder()
                .authorizationId(authorization.getId())
                .companyId(authorization.getCompanyId())
                .newBuyer(newBuyer)
                .oldBuyer(oldBuyer)
                .userId(userId)
                .updateDate(LocalDateTime.now())
                .changeNumber(changeNumber)
                .build();
        
        historyRepository.save(history);
    }
    
    public List<AuthorizationHistory> getHistoryByAuthorizationId(Long authorizationId) {
        return historyRepository.findByAuthorizationIdOrderByChangeNumberDesc(authorizationId);
    }
}