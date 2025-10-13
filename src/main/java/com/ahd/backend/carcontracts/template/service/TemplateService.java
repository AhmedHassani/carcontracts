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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final CompanyRepository companyRepository;
    private final Helper helper;
    private final S3FileStorageService s3FileStorageService;

//    @Transactional
//    public TemplateDTO saveTemplate(TemplateDTO dto) {
//        long companyId = helper.getCurrentCompanyId();
//        Company company = companyRepository.findById(companyId)
//                .orElseThrow(() -> new RuntimeException("Company not found"));
//
//        Template entity = TemplateMapper.toEntity(dto, company);
//        Template saved = templateRepository.save(entity);
//        return TemplateMapper.toDTO(saved);
//    }

    @Transactional(readOnly = true)
    public TemplateDTO getTemplate(Long id) {
        long companyId = helper.getCurrentCompanyId();
        Template template = templateRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        return TemplateMapper.toDTO(template);
    }

    @Transactional(readOnly = true)
    public List<TemplateDTO> getTemplatesByCompany() {
        long companyId = helper.getCurrentCompanyId();
        return templateRepository.findByCompanyIdOrderByUpdatedAtDesc(companyId)
                .stream().map(TemplateMapper::toDTO).toList();
    }

    // TemplateService.java (مقتطفات مهمة)
    @Transactional
    public TemplateDTO updateTemplate(Long id, TemplateDTO dto) {
        long companyId = helper.getCurrentCompanyId();
        Template template = templateRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        template.setName(dto.getName());
        template.setImageKey(dto.getImageKey());
        template.setCompany(company);

        if (template.getFields() == null) {
            template.setFields(new ArrayList<>()); // <= مهم
        } else {
            template.getFields().clear();
        }

        if (dto.getFields() != null) {
            var fields = dto.getFields().stream()
                    .map(TemplateFieldMapper::toEntity)
                    .peek(f -> f.setTemplate(template))
                    .toList();
            template.getFields().addAll(fields);
        }

        return TemplateMapper.toDTO(templateRepository.save(template));
    }

    @Transactional
    public TemplateDTO saveTemplate(TemplateDTO dto) {
        long companyId = helper.getCurrentCompanyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Template entity = TemplateMapper.toEntity(dto, company);
        // حماية إضافية
        if (entity.getFields() == null) {
            entity.setFields(new ArrayList<>());
        } else {
            // تأكد من ضبط الـ back-reference
            entity.getFields().forEach(f -> f.setTemplate(entity));
        }
        Template saved = templateRepository.save(entity);
        return TemplateMapper.toDTO(saved);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        long companyId = helper.getCurrentCompanyId();
        Template template = templateRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        // optionally delete background image from S3
        String key = template.getImageKey();
        templateRepository.delete(template);
        if (key != null && !key.isBlank()) {
            try { s3FileStorageService.delete(key); } catch (Exception ignored) {}
        }
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
        Template template = templateRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // upload new first
        String newKey = s3FileStorageService.upload(image);
        String oldKey = template.getImageKey();

        template.setName(dto.getName());
        template.setImageKey(newKey);
        template.setCompany(company);

        template.getFields().clear();
        if (dto.getFields() != null) {
            var fields = dto.getFields().stream()
                    .map(TemplateFieldMapper::toEntity)
                    .peek(f -> f.setTemplate(template))
                    .toList();
            template.getFields().addAll(fields);
        }

        TemplateDTO out = TemplateMapper.toDTO(templateRepository.save(template));

        if (oldKey != null && !oldKey.isBlank()) {
            try { s3FileStorageService.delete(oldKey); } catch (Exception ignored) {}
        }
        return out;
    }
}
