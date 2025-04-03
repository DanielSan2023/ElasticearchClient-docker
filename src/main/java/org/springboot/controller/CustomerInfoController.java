package org.springboot.controller;

import org.springboot.exception.ProductNotFoundException;
import org.springboot.model.CustomerInfo;
import org.springboot.service.CustomerInfoService;
import org.springboot.service.ElasticsearchServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerInfoController {
    private final CustomerInfoService customerInfoService;
    private final ElasticsearchServiceImpl elasticsearchService;

    public CustomerInfoController(CustomerInfoService customerInfoService, ElasticsearchServiceImpl elasticsearchService) {
        this.customerInfoService = customerInfoService;
        this.elasticsearchService = elasticsearchService;
    }

    @PostMapping
    public ResponseEntity<CustomerInfo> createCustomerInfo(@RequestBody CustomerInfo customer) {
        CustomerInfo savedCustomer = customerInfoService.saveCustomer(customer);
        return new ResponseEntity<>(savedCustomer, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CustomerInfo>> getAllCustomers() {
        List<CustomerInfo> allCustomers = elasticsearchService.getAll("customers-002", CustomerInfo.class);
        return new ResponseEntity<>(allCustomers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerInfo> getCustomerById(@PathVariable String id) {
        CustomerInfo customer = elasticsearchService.getById("customers-002", id, CustomerInfo.class);
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerInfo> addOrderCustomer(@PathVariable String id, @RequestBody String orderId) throws ProductNotFoundException {
        CustomerInfo updatedCustomer = customerInfoService.addOrderToCustomer(id, orderId);
        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }
}
