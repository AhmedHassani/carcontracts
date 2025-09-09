package com.ahd.backend.carcontracts.template.controller;



import com.ahd.backend.carcontracts.template.dto.TemplateDTO;
import com.ahd.backend.carcontracts.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${application.api.base-path}/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public TemplateDTO create(@RequestBody TemplateDTO dto) {
        return templateService.saveTemplate(dto);
    }

    @GetMapping("/{id}")
    public TemplateDTO getById(@PathVariable Long id) {
        return templateService.getTemplate(id);
    }

    @GetMapping("/company")
    public List<TemplateDTO> getByCompany() {
        return templateService.getTemplatesByCompany();
    }

    @PutMapping("/{id}")
    public TemplateDTO update(@PathVariable Long id, @RequestBody TemplateDTO dto) {
        return templateService.updateTemplate(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        templateService.deleteTemplate(id);
    }

    @PostMapping("/with-image")
    public TemplateDTO createWithImage(@RequestPart("template") TemplateDTO dto,
                                       @RequestPart("image") MultipartFile image) {
        return templateService.saveTemplateWithImage(dto, image);
    }

    @PutMapping("/{id}/with-image")
    public TemplateDTO updateWithImage(@PathVariable Long id,
                                       @RequestPart("template") TemplateDTO dto,
                                       @RequestPart("image") MultipartFile image) {
        return templateService.updateTemplateWithImage(id, dto, image);
    }
}
