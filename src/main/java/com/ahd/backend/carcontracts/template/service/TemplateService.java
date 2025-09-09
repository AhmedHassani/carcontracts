package com.ahd.backend.carcontracts.template.service;


import com.ahd.backend.carcontracts.S3.S3FileStorageService;
import com.ahd.backend.carcontracts.company.model.Company;
import com.ahd.backend.carcontracts.company.repository.CompanyRepository;
import com.ahd.backend.carcontracts.template.dto.TemplateDTO;
import com.ahd.backend.carcontracts.template.mapper.TemplateFieldMapper;
import com.ahd.backend.carcontracts.template.mapper.TemplateMapper;
import com.ahd.backend.carcontracts.template.model.Template;
import com.ahd.backend.carcontracts.template.repository.TemplateRepository;
import com.ahd.backend.carcontracts.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final CompanyRepository companyRepository;
    private final Helper helper;
    private final S3FileStorageService s3FileStorageService;

    @Transactional
    public TemplateDTO saveTemplate(TemplateDTO dto) {
        long  companyId = helper.getCurrentCompanyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        Template entity = TemplateMapper.toEntity(dto, company);
        Template saved = templateRepository.save(entity);
        return TemplateMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public TemplateDTO getTemplate(Long id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        return TemplateMapper.toDTO(template);
    }

    @Transactional(readOnly = true)
    public List<TemplateDTO> getTemplatesByCompany() {
        long  companyId = helper.getCurrentCompanyId();
        return templateRepository.findByCompanyId(companyId).stream()
                .map(TemplateMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TemplateDTO updateTemplate(Long id, TemplateDTO dto) {
        long  companyId = helper.getCurrentCompanyId();
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        template.setName(dto.getName());
        template.setImageKey(dto.getImageKey());
        template.setCompany(company);
        template.getFields().clear();
        if (dto.getFields() != null) {
            var fields = dto.getFields().stream()
                    .map(TemplateFieldMapper::toEntity)
                    .collect(Collectors.toList());
            fields.forEach(f -> f.setTemplate(template));
            template.getFields().addAll(fields);
        }
        return TemplateMapper.toDTO(templateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    @Transactional
    public TemplateDTO saveTemplateWithImage(TemplateDTO dto, MultipartFile image) {
        long companyId = helper.getCurrentCompanyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        String imageKey = s3FileStorageService.upload(image);
        dto.setImageKey(imageKey);
        Template entity = TemplateMapper.toEntity(dto, company);
        Template saved = templateRepository.save(entity);
        return TemplateMapper.toDTO(saved);
    }

    @Transactional
    public TemplateDTO updateTemplateWithImage(Long id, TemplateDTO dto, MultipartFile image) {
        long companyId = helper.getCurrentCompanyId();
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        // Optionally delete old image from S3 if it exists
        if (template.getImageKey() != null) {
            s3FileStorageService.delete(template.getImageKey());
        }
        String imageKey = s3FileStorageService.upload(image);
        template.setName(dto.getName());
        template.setImageKey(imageKey);
        template.setCompany(company);
        template.getFields().clear();
        if (dto.getFields() != null) {
            var fields = dto.getFields().stream()
                    .map(TemplateFieldMapper::toEntity)
                    .collect(Collectors.toList());
            fields.forEach(f -> f.setTemplate(template));
            template.getFields().addAll(fields);
        }
        return TemplateMapper.toDTO(templateRepository.save(template));
    }
}
