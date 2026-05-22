package com.ahd.backend.carcontracts.appuser.dto;
import lombok.*;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkPermissionsRequest {
    private List<Long> permissionIds;
}