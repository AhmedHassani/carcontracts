package com.ahd.backend.carcontracts.users;

import com.ahd.backend.carcontracts.company.Company;
import com.ahd.backend.carcontracts.company.CompanyRepository;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public UserResponseDTO createUser(UserRequestDTO dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        User user = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .role(dto.getRole())
                .company(company)
                .createdAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);
        return mapToDto(user);
    }


    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }


    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDto(user);
    }


    private UserResponseDTO mapToDto(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .build();
    }


}
