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
    private int page = 0;
    private int size = 20;
}

