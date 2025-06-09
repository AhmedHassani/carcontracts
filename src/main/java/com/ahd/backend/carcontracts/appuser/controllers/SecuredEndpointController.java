package com.ahd.backend.carcontracts.appuser.controllers;

import com.ahd.backend.carcontracts.appuser.models.SecuredEndpoint;
import com.ahd.backend.carcontracts.appuser.services.SecuredEndpointService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secured-endpoints")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SecuredEndpointController {

    private final SecuredEndpointService svc;

    public SecuredEndpointController(SecuredEndpointService svc) {
        this.svc = svc;
    }

    @GetMapping
    public List<SecuredEndpoint> list() {
        return svc.findAll();
    }

    @PostMapping
    public SecuredEndpoint create(@RequestBody SecuredEndpoint dto) {
        return svc.create(dto);
    }

    @PutMapping("/{id}")
    public SecuredEndpoint update(@PathVariable Long id,
                                  @RequestBody SecuredEndpoint dto) {
        return svc.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }
}
