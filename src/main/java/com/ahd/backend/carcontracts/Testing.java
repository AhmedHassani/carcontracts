package com.ahd.backend.carcontracts;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class Testing {

    @GetMapping(value = {"/",""})
    public String testMethod(){
        return  "Hello";
    }

}
