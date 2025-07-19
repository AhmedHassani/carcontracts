package com.ahd.backend.carcontracts.company.dto;

public class CompanyUserSearchCriteria {

    private String sortBy       = "user.id";
    private String sortDirection= "asc";
    private String keyword      = "";

    // no-arg constructor for data binding
    public CompanyUserSearchCriteria() { }

    // all-args constructor (optional)
    public CompanyUserSearchCriteria(String sortBy,
                                     String sortDirection,
                                     String keyword) {
        this.sortBy        = sortBy != null        ? sortBy        : this.sortBy;
        this.sortDirection = sortDirection != null ? sortDirection : this.sortDirection;
        this.keyword       = keyword != null       ? keyword       : this.keyword;
    }

    // getters & setters
    public String getSortBy()         { return sortBy; }
    public void setSortBy(String s)   { this.sortBy = s; }
    public String getSortDirection()  { return sortDirection; }
    public void setSortDirection(String d) { this.sortDirection = d; }
    public String getKeyword()        { return keyword; }
    public void setKeyword(String k)  { this.keyword = k; }
}