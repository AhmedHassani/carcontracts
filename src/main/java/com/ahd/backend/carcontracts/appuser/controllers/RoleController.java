package com.ahd.backend.carcontracts.appuser.controllers;


import com.ahd.backend.carcontracts.appuser.models.Role;
import com.ahd.backend.carcontracts.appuser.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService svc;

    @GetMapping(value = {"/",""})
    public List<Role> list() {
        return svc.findAll();
    }

    @PostMapping
    public Role create(@RequestBody Role r) {
        return svc.create(r);
    }

    @PutMapping("/{id}")
    public Role update(@PathVariable Long id, @RequestBody Role r) {
        return svc.update(id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }
}
