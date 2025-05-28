package com.ahd.backend.carcontracts.company;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class CompanySearchCriteria {
    private String searchTerm; // For companyName or ownerName
    private CompanyStatus status;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate subscriptionDateFrom;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate subscriptionDateTo;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDateFrom;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDateTo;
    
    private String sortBy = "id"; // Default sort field
    private String sortDirection = "ASC"; // Default sort direction
} 