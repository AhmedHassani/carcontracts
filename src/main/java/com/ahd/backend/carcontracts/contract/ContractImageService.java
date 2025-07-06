package com.ahd.backend.carcontracts.contract;

import com.ahd.backend.carcontracts.car.Car;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.ahd.backend.carcontracts.S3.ImageStorageService;
import com.ahd.backend.carcontracts.S3.S3UrlService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ContractImageService {

    private final ContractImageRepository contractImageRepository;
    private final ContractRepository contractRepository;
    private final ImageStorageService imageStorageService;

    private final S3UrlService s3UrlService;


    public ContractImageResponseDTO updateContractImage(Long id, ContractImageDTO dto) {
        Contract existing = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found with id " + id));

        String imageKey = imageStorageService.upload(dto.getImage());

        ContractImage contractImage = ContractImage.builder()
                .contract(existing)
                .image(imageKey)
                .deleted(dto.isDeleted())
                .build();

        ContractImage saved = contractImageRepository.save(contractImage);

        return new ContractImageResponseDTO(
                saved.getId(),
                saved.getImage(),
                saved.isDeleted()
        );
    }



    public void deleteContractImage(Long id) {
        ContractImage existing = contractImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image not found with id " + id));

        existing.setDeleted(true);
        contractImageRepository.save(existing);
    }

    public List<ContractImageResponseDTO> getImagesByContractId(Long contractId) {
        return contractImageRepository.findByContractIdAndDeletedFalse(contractId)
                .stream()
                .map(image -> {
                    String imageUrl = s3UrlService.getImageUrl(image.getImage());
                    return new ContractImageResponseDTO(
                            image.getId(),
                            imageUrl,
                            image.isDeleted()
                    );
                })
                .collect(Collectors.toList());
    }



}
