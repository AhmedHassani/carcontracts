package com.ahd.backend.carcontracts.appuser.repository;


import java.util.List;

import com.ahd.backend.carcontracts.appuser.models.SecuredEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecuredEndpointRepository extends JpaRepository<SecuredEndpoint, Long> {
    List<SecuredEndpoint> findByHttpMethod(String httpMethod);
}
