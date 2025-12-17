package com.ahd.backend.carcontracts.dropDownList.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOptionRequest {
    private Long id; // For bulk updates
    private String label;
    private String value;
    private Long sub;
    private Long root;

    public boolean isEmpty() {
        return (label == null || label.trim().isEmpty()) &&
                (value == null || value.trim().isEmpty());
    }
}