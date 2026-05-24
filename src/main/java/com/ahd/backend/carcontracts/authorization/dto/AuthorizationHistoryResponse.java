package com.ahd.backend.carcontracts.authorization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationHistoryResponse {
    private String userName;        
    private String newBuyerName;    
    private String oldBuyerName;    
    private LocalDateTime updateDate; 
}