package org.springboot.controller;

import org.springboot.model.CustomerInfo;
import org.springboot.service.CustomerInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerInfoController {
    private final CustomerInfoService customerInfoService;

    public CustomerInfoController(CustomerInfoService customerInfoService) {
        this.customerInfoService = customerInfoService;
    }

    @PostMapping
    public ResponseEntity<CustomerInfo> createCustomerInfo(@RequestBody CustomerInfo customer) {
        CustomerInfo savedCustomer = customerInfoService.saveCustomer(customer);
        return new ResponseEntity<>(savedCustomer, HttpStatus.OK);
    }




}
