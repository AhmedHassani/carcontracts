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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Transactional(readOnly = true)
    public List<DropDownResponseDTO> getAllDropDowns() {

        List<DropDown> dropDowns = dropDownRepository.findAll();
        return dropDownMapper.toDropDownResponseDTOList(dropDowns);
    }

    @Transactional(readOnly = true)
    public List<OptionResponseDTO> getOptionsByDropDownId(Long dropDownId , Long root ) {
        List<OptionDropDown> optionDropDown = null;
        if(root == 0){
            optionDropDown =
                    optionDropDownRepository.findBydropDownId(dropDownId);
        }else{
            optionDropDown =
                    optionDropDownRepository.findBydropDownIdAndSub(dropDownId , root);
        }

        return dropDownMapper.toOptionResponseDTOList(optionDropDown);
    }

    @Transactional
    //@Auditable(operation = "انشاء خيار في القائمة المنسدلة", captureArgs = true, captureResult = true)
    public OptionResponseDTO createOption(CreateOptionRequest request) {
       // log.debug("Creating option in dropdown id: {}", request.getDropDownId());

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
        return dropDownMapper.toOptionResponseDTO(savedOption);
    }

    @Transactional
    public OptionResponseDTO updateOption(Long optionId, UpdateOptionRequest request) {

        if (request == null || request.isEmpty()) {
            throw new BadRequestException("Update payload must contain at least one field");
        }

        OptionDropDown option = optionDropDownRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Option with id " + optionId + " not found"));


        if (request.getLabel() != null) {
            option.setLabel(request.getLabel());
        }

        if (request.getValue() != null) {
            option.setValue(request.getValue());
        }

        OptionDropDown updatedOption = optionDropDownRepository.save(option);
        return dropDownMapper.toOptionResponseDTO(updatedOption);
    }

    @Transactional
   // @Auditable(operation = "حذف خيار من القائمة المنسدلة", captureArgs = true, captureResult = true)
    public void deleteOption(Long optionId) {
        //log.debug("Deleting option id: {}", optionId);

        OptionDropDown option = optionDropDownRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Option with id " + optionId + " not found"));

        optionDropDownRepository.delete(option);

       // log.info("Option deleted successfully with id: {}", optionId);
    }

}