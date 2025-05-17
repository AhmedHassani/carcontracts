package com.ahd.backend.carcontracts.util.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
public class PageRequestParams {
    private String search;              // search keywords (optional)
    private int page = 0;               // page number (0-indexed)
    private int size = 20;             // page size
    private String sortBy = "id";       // default sort field
    private String sortDirection = "ASC"; // "ASC" or "DESC"
    private List<FilterCriteria> filters = new ArrayList<>();

}

