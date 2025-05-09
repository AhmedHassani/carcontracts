package com.ahd.backend.carcontracts.appuser.services;



import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository repo;


    public List<Role> findAll() { return repo.findAll(); }

    public Role create(Role r) {
        if (repo.findByName(r.getName()).isPresent()) {
            throw new IllegalArgumentException("Role already exists");
        }
        return repo.save(r);
    }

    public Role update(Long id, Role r) {
        Role existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        existing.setName(r.getName());
        existing.setPermissions(r.getPermissions());
        return repo.save(existing);
    }

    public void delete(Long id) { repo.deleteById(id); }
}
