package com.ahd.backend.carcontracts.util.base;

import lombok.Getter;
import org.springframework.data.domain.Page;

public class Pagination{
    private Page page;
    @Getter
    private int currentPage;
    @Getter
    private int lastPage;
    @Getter
    private long totalElements;

    public Pagination(Page page) {
        this.page = page;
        currentPage = this.page.getNumber();
        lastPage= this.page.getTotalPages();
        totalElements = this.getTotalElements();
    }

}
