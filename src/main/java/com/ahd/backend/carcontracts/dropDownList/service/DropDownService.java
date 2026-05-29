package com.ahd.backend.carcontracts.dropDownList.service;

import com.ahd.backend.carcontracts.audit.Auditable;
import com.ahd.backend.carcontracts.dropDownList.dto.CreateOptionRequest;
import com.ahd.backend.carcontracts.dropDownList.dto.DropDownResponseDTO;
import com.ahd.backend.carcontracts.dropDownList.dto.OptionResponseDTO;
import com.ahd.backend.carcontracts.dropDownList.dto.UpdateOptionRequest;
import com.ahd.backend.carcontracts.dropDownList.mapper.DropDownMapper;
import com.ahd.backend.carcontracts.dropDownList.model.DropDown;
import com.ahd.backend.carcontracts.dropDownList.model.OptionDropDown;
import com.ahd.backend.carcontracts.dropDownList.repository.DropDownRepository;
import com.ahd.backend.carcontracts.dropDownList.repository.OptionDropDownRepository;
import com.ahd.backend.carcontracts.exception.BadRequestException;
import com.ahd.backend.carcontracts.exception.DuplicateResourceException;
import com.ahd.backend.carcontracts.exception.ResourceNotFoundException;
import com.ahd.backend.carcontracts.notification.dto.NotificationContext;
import com.ahd.backend.carcontracts.notification.service.NotificationSender;
import com.ahd.backend.carcontracts.notification.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DropDownService {

    private final DropDownRepository dropDownRepository;
    private final OptionDropDownRepository optionDropDownRepository;
    private final DropDownMapper dropDownMapper;
    
    // ✅ ADD NOTIFICATION DEPENDENCIES
    private final NotificationSender notificationSender;
    private final MessageService messageService;

    // ==================== HELPER METHODS FOR NOTIFICATIONS ====================
    
    /**
     * Copy option for change tracking
     */
    private OptionDropDown copyOption(OptionDropDown option) {
        if (option == null) return null;
        
        return OptionDropDown.builder()
                .id(option.getId())
                .label(option.getLabel())
                .value(option.getValue())
                .dropDownId(option.getDropDownId())
                .root(option.getRoot())
                .sub(option.getSub())
                .build();
    }
    
    /**
     * Get dropdown name by ID
     */
    private String getDropDownName(Long dropDownId) {
        return dropDownRepository.findById(dropDownId)
                .map(DropDown::getName)
                .orElse("غير معروف");
    }

    @Transactional(readOnly = true)
    public List<DropDownResponseDTO> getAllDropDowns() {
        List<DropDown> dropDowns = dropDownRepository.findAll();
        return dropDownMapper.toDropDownResponseDTOList(dropDowns);
    }

    @Transactional(readOnly = true)
    public List<OptionResponseDTO> getOptionsByDropDownId(Long dropDownId, Long root) {
        List<OptionDropDown> optionDropDown = null;
        if (root == 0) {
            optionDropDown = optionDropDownRepository.findBydropDownId(dropDownId);
        } else {
            optionDropDown = optionDropDownRepository.findBydropDownIdAndSub(dropDownId, root);
        }
        return dropDownMapper.toOptionResponseDTOList(optionDropDown);
    }

    @Transactional
    @Auditable(operation = "انشاء خيار في القائمة المنسدلة", captureArgs = true, captureResult = true)
    public OptionResponseDTO createOption(CreateOptionRequest request) {
        DropDown dropDown = dropDownRepository.findById(request.getDropDownId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dropdown with id " + request.getDropDownId() + " not found"));

        OptionDropDown option = OptionDropDown.builder()
                .label(request.getLabel())
                .value(request.getValue())
                .dropDownId(request.getDropDownId())
                .sub(request.getSub())
                .root(request.getRoot())
                .build();

        OptionDropDown savedOption = optionDropDownRepository.save(option);
        
        // ✅ ADD NOTIFICATION FOR OPTION CREATION
        String dropDownName = getDropDownName(request.getDropDownId());
        NotificationContext context = notificationSender.createDropDownContext("CREATE", savedOption, dropDownName);
        notificationSender.notifyDropDownOperation(context);
        
        return dropDownMapper.toOptionResponseDTO(savedOption);
    }

    @Transactional
    @Auditable(operation = "تحديث خيار في القائمة المنسدلة", captureArgs = true, captureResult = true)
    public OptionResponseDTO updateOption(Long optionId, UpdateOptionRequest request) {
        if (request == null || request.isEmpty()) {
            throw new BadRequestException("Update payload must contain at least one field");
        }

        OptionDropDown option = optionDropDownRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Option with id " + optionId + " not found"));
        
        // ✅ Store old option for change tracking
        OptionDropDown oldOption = copyOption(option);
        String dropDownName = getDropDownName(option.getDropDownId());

        if (request.getLabel() != null) {
            option.setLabel(request.getLabel());
        }

        if (request.getValue() != null) {
            option.setValue(request.getValue());
        }

        OptionDropDown updatedOption = optionDropDownRepository.save(option);
        
        // ✅ ADD NOTIFICATION FOR OPTION UPDATE
        String changeDetails = generateOptionChangeDetails(oldOption, updatedOption);
        NotificationContext context = notificationSender.createDropDownContext("UPDATE", updatedOption, dropDownName, changeDetails);
        notificationSender.notifyDropDownOperation(context);
        
        return dropDownMapper.toOptionResponseDTO(updatedOption);
    }

    @Transactional
    @Auditable(operation = "حذف خيار من القائمة المنسدلة", captureArgs = true, captureResult = true)
    public void deleteOption(Long optionId) {
        OptionDropDown option = optionDropDownRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Option with id " + optionId + " not found"));
        
        // ✅ Store option info before deletion for notification
        OptionDropDown optionToDelete = copyOption(option);
        String dropDownName = getDropDownName(option.getDropDownId());

        optionDropDownRepository.delete(option);
        
        // ✅ ADD NOTIFICATION FOR OPTION DELETION
        NotificationContext context = notificationSender.createDropDownContext("DELETE", optionToDelete, dropDownName);
        notificationSender.notifyDropDownOperation(context);
    }
    
    /**
     * Generate change details for option update
     */
    private String generateOptionChangeDetails(OptionDropDown oldOption, OptionDropDown newOption) {
        List<String> changes = new java.util.ArrayList<>();
        
        // Check label change
        if (!Objects.equals(oldOption.getLabel(), newOption.getLabel())) {
            changes.add(messageService.getMessage("notification.dropdown.change.label",
                oldOption.getLabel() != null ? oldOption.getLabel() : "غير محدد",
                newOption.getLabel() != null ? newOption.getLabel() : "غير محدد"));
        }
        
        // Check value change
        if (!Objects.equals(oldOption.getValue(), newOption.getValue())) {
            changes.add(messageService.getMessage("notification.dropdown.change.value",
                oldOption.getValue() != null ? oldOption.getValue() : "غير محدد",
                newOption.getValue() != null ? newOption.getValue() : "غير محدد"));
        }
        
        if (changes.isEmpty()) {
            changes.add(messageService.getMessage("notification.dropdown.change.default"));
        }
        
        return String.join("\n", changes);
    }
}