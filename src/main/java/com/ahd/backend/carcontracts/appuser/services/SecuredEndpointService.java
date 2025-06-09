package com.ahd.backend.carcontracts.appuser.services;


import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.models.SecuredEndpoint;
import com.ahd.backend.carcontracts.appuser.repository.RoleRepository;
import com.ahd.backend.carcontracts.appuser.repository.SecuredEndpointRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SecuredEndpointService {

    private final SecuredEndpointRepository endpointRepo;
    private final RoleRepository roleRepo;

    public SecuredEndpointService(SecuredEndpointRepository endpointRepo,
                                  RoleRepository roleRepo) {
        this.endpointRepo = endpointRepo;
        this.roleRepo = roleRepo;
    }

    public List<SecuredEndpoint> findAll() {
        return endpointRepo.findAll();
    }

    public SecuredEndpoint create(SecuredEndpoint dto) {
        Role role = roleRepo.findById(dto.getRole().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Role not found with id " + dto.getRole().getId()));
        SecuredEndpoint se = SecuredEndpoint.builder()
                .httpMethod(dto.getHttpMethod())
                .pattern(dto.getPattern())
                .role(role)
                .build();
        return endpointRepo.save(se);
    }

    public SecuredEndpoint update(Long id, SecuredEndpoint dto) {
        SecuredEndpoint existing = endpointRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "SecuredEndpoint not found with id " + id));
        existing.setHttpMethod(dto.getHttpMethod());
        existing.setPattern(dto.getPattern());

        Role role = roleRepo.findById(dto.getRole().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Role not found with id " + dto.getRole().getId()));
        existing.setRole(role);

        return endpointRepo.save(existing);
    }

    public void delete(Long id) {
        if (!endpointRepo.existsById(id)) {
            throw new EntityNotFoundException("SecuredEndpoint not found with id " + id);
        }
        endpointRepo.deleteById(id);
    }
}
