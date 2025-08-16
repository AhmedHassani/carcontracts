package com.ahd.backend.carcontracts.authorization.service;

import com.ahd.backend.carcontracts.authorization.dto.AuthorizationResponse;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationSearchCriteria;
import com.ahd.backend.carcontracts.authorization.dto.AuthorizationUpsertRequest;
import com.ahd.backend.carcontracts.authorization.mapper.AuthorizationMapper;
import com.ahd.backend.carcontracts.authorization.model.Authorization;
import com.ahd.backend.carcontracts.authorization.repository.AuthorizationRepository;
import com.ahd.backend.carcontracts.car.model.Car;
import com.ahd.backend.carcontracts.car.repository.CarRepository;
import com.ahd.backend.carcontracts.person.model.Person;
import com.ahd.backend.carcontracts.person.repository.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorizationService {

    private final AuthorizationRepository authorizationRepository;
    private final PersonRepository personRepository;
    private final CarRepository carRepository;

    @Transactional(readOnly = true)
    public Page<AuthorizationResponse> list(Pageable pageable, AuthorizationSearchCriteria searchCriteria) {
        return authorizationRepository.findAll(pageable)
                .map(AuthorizationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AuthorizationResponse get(Long id) {
        Authorization entity = authorizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Authorization not found: " + id));
        return AuthorizationMapper.toResponse(entity);
    }

    public AuthorizationResponse create(AuthorizationUpsertRequest r) {
        if (authorizationRepository.existsByAuthorizationNumber(r.getAuthorizationNumber())) {
            throw new IllegalArgumentException("authorizationNumber already exists");
        }
        Person buyer = personRepository.findById(r.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found: " + r.getBuyerId()));
        Car car = carRepository.findById(r.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + r.getCarId()));
        Authorization entity = AuthorizationMapper.fromUpsertRequest(r, buyer, car);
        return AuthorizationMapper.toResponse(authorizationRepository.save(entity));
    }

    public AuthorizationResponse update(Long id, AuthorizationUpsertRequest r) {
        Authorization entity = authorizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Authorization not found: " + id));
        if (!entity.getAuthorizationNumber().equals(r.getAuthorizationNumber())
                && authorizationRepository.existsByAuthorizationNumber(r.getAuthorizationNumber())) {
            throw new IllegalArgumentException("authorizationNumber already exists");
        }
        Person buyer = personRepository.findById(r.getBuyerId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer not found: " + r.getBuyerId()));
        Car car = carRepository.findById(r.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + r.getCarId()));
        AuthorizationMapper.update(entity, r, buyer, car);
        return AuthorizationMapper.toResponse(authorizationRepository.save(entity));
    }

    public void delete(Long id) {
        if (!authorizationRepository.existsById(id)) {
            throw new EntityNotFoundException("Authorization not found: " + id);
        }
        authorizationRepository.deleteById(id);
    }



}
