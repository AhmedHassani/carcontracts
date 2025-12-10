package com.ahd.backend.carcontracts.dropDownList.mapper;

import com.ahd.backend.carcontracts.dropDownList.dto.*;
import com.ahd.backend.carcontracts.dropDownList.model.DropDown;
import com.ahd.backend.carcontracts.dropDownList.model.OptionDropDown;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DropDownMapper {

    // ==================== TO DTO ====================

    public OptionResponseDTO toOptionResponseDTO(OptionDropDown option) {
        if (option == null) {
            return null;
        }

        return OptionResponseDTO.builder()
                .id(option.getId())
                .label(option.getLabel())
                .value(option.getValue())
                .dropDownId(option.getDropDownId())
                .build();
    }

    public List<OptionResponseDTO> toOptionResponseDTOList(List<OptionDropDown> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }

        return options.stream()
                .map(this::toOptionResponseDTO)
                .collect(Collectors.toList());
    }

    public DropDownResponseDTO toDropDownResponseDTO(DropDown dropDown) {
        if (dropDown == null) {
            return null;
        }

        return DropDownResponseDTO.builder()
                .id(dropDown.getId())
                .name(dropDown.getName())
                .build();
    }

    public List<DropDownResponseDTO> toDropDownResponseDTOList(List<DropDown> dropDowns) {
        if (dropDowns == null || dropDowns.isEmpty()) {
            return Collections.emptyList();
        }

        return dropDowns.stream()
                .map(this::toDropDownResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== TO ENTITY ====================

    public OptionDropDown toOptionEntity(CreateOptionRequest request) {
        if (request == null) {
            return null;
        }

        return OptionDropDown.builder()
                .label(request.getLabel())
                .value(request.getValue())
                .build();
    }

    public OptionDropDown toOptionEntity(OptionResponseDTO dto) {
        if (dto == null) {
            return null;
        }

        return OptionDropDown.builder()
                .id(dto.getId())
                .label(dto.getLabel())
                .value(dto.getValue())
                .build();
    }

    public DropDown toDropDownEntity(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        return DropDown.builder()
                .name(name)
                .build();
    }

    public DropDown toDropDownEntity(DropDownResponseDTO dto) {
        if (dto == null) {
            return null;
        }

        return DropDown.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }

    // ==================== UPDATE ENTITY ====================

    public void updateOptionEntity(OptionDropDown option, UpdateOptionRequest request) {
        if (option == null || request == null) {
            return;
        }

        if (request.getLabel() != null) {
            option.setLabel(request.getLabel());
        }

        if (request.getValue() != null) {
            option.setValue(request.getValue());
        }
    }

//    public void updateDropDownEntity(DropDown dropDown, UpdateDropDownRequest request) {
//        if (dropDown == null || request == null) {
//            return;
//        }
//
//        if (request.getName() != null) {
//            dropDown.setName(request.getName());
//        }
//    }

    // ==================== SIMPLIFIED DTOs (For basic operations) ====================

    public OptionDropDownDTO toOptionDropDownDTO(OptionDropDown option) {
        if (option == null) {
            return null;
        }

        return OptionDropDownDTO.builder()
                .id(option.getId())
                .label(option.getLabel())
                .value(option.getValue())
                .build();
    }

//    public List<OptionDropDownDTO> toOptionDropDownDTOList(List<OptionDropDown> options) {
//        if (options == null || options.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        return options.stream()
//                .map(this::toOptionDropDownDTO)
//                .collect(Collectors.toList());
//    }
//
//    public DropDownDTO toDropDownDTO(DropDown dropDown) {
//        if (dropDown == null) {
//            return null;
//        }
//
//        return DropDownDTO.builder()
//                .id(dropDown.getId())
//                .name(dropDown.getName())
//                .options(toOptionDropDownDTOList(dropDown.getOptions()))
//                .build();
//    }

//    public List<DropDownDTO> toDropDownDTOList(List<DropDown> dropDowns) {
//        if (dropDowns == null || dropDowns.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        return dropDowns.stream()
//                .map(this::toDropDownDTO)
//                .collect(Collectors.toList());
//    }

    // ==================== FROM SIMPLIFIED DTOs ====================

    public OptionDropDown toOptionEntity(OptionDropDownDTO dto) {
        if (dto == null) {
            return null;
        }

        return OptionDropDown.builder()
                .id(dto.getId())
                .label(dto.getLabel())
                .value(dto.getValue())
                .build();
    }

    public DropDown toDropDownEntity(DropDownDTO dto) {
        if (dto == null) {
            return null;
        }

        return DropDown.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}