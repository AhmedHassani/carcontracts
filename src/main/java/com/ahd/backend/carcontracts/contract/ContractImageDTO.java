package com.ahd.backend.carcontracts.contract;

import com.ahd.backend.carcontracts.S3.S3UrlService;
import com.ahd.backend.carcontracts.config.ApplicationContextProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractImageDTO {
    private Long id;
    private String image;
    private boolean deleted;
}

