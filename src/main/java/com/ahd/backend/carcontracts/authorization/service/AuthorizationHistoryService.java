package com.ahd.backend.carcontracts.authorization.service;

import com.ahd.backend.carcontracts.appuser.models.AppUser;
import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationHistoryResponse;
import com.ahd.backend.carcontracts.authorization.mapper.AuthorizationHistoryMapper;
import com.ahd.backend.carcontracts.authorization.model.Authorization;
import com.ahd.backend.carcontracts.authorization.model.AuthorizationHistory;
import com.ahd.backend.carcontracts.authorization.repository.AuthorizationHistoryRepository;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import com.ahd.backend.carcontracts.util.Helper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthorizationHistoryService {

    private final AuthorizationHistoryRepository historyRepository;
    private final PersonRepository personRepository;
    private final UserRepository appUserRepository;
    private final Helper helper;

    private Integer getNextChangeNumber(Long authorizationId) {
        Integer maxChangeNumber = historyRepository.findMaxChangeNumberByAuthorizationId(authorizationId);
        return (maxChangeNumber == null) ? 1 : maxChangeNumber + 1;
    }

    public void createHistoryEntry(Authorization authorization, Long newBuyerId, Long oldBuyerId) {
        // Get current user ID from Helper
        Long userId = helper.getCurrentUserId();
        
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
        log.info("Created history entry for authorization {} with change number {} by user {}", 
                 authorization.getId(), changeNumber, userId);
    }
    
    @Transactional(readOnly = true)
    public List<AuthorizationHistory> getHistoryByAuthorizationId(Long authorizationId) {
        return historyRepository.findByAuthorizationIdOrderByChangeNumberDesc(authorizationId);
    }
    
    @Transactional(readOnly = true)
    public List<AuthorizationHistoryResponse> getHistoryResponseByAuthorizationId(Long authorizationId) {
        List<AuthorizationHistory> historyList = getHistoryByAuthorizationId(authorizationId);
        
        return historyList.stream()
                .map(history -> {
                    String userName = null;
                    if (history.getUserId() != null) {
                        userName = appUserRepository.findById(history.getUserId())
                                .map(AppUser::getUsername)
                                .orElse("Unknown User");
                    }
                    return AuthorizationHistoryMapper.toResponse(history, userName);
                })
                .collect(Collectors.toList());
    }
}