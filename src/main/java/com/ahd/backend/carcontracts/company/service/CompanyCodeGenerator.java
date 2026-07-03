package com.ahd.backend.carcontracts.company.service;

import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class CompanyCodeGenerator {
    
    private final CompanyRepository companyRepository;
    private final Lock lock = new ReentrantLock();
    
    public String generateNextCode() {
        lock.lock();
        try {
            String maxCode = companyRepository.findMaxCode();
            
            if (maxCode == null || maxCode.isEmpty()) {
                return "001"; // Start with 001 if no records exist
            }
            
            // Parse the numeric part and increment
            int numericValue = Integer.parseInt(maxCode);
            numericValue++;
            
            // Format back to 3 digits with leading zeros
            return String.format("%03d", numericValue);
        } finally {
            lock.unlock();
        }
    }
}