package org.springboot.controller;

import org.springboot.model.CustomerInfo;
import org.springboot.service.CustomerInfoService;
import org.springboot.service.ElasticsearchServiceImpl;
import org.springboot.utility.AppConstants;
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
        List<CustomerInfo> allCustomers = elasticsearchService.getAll(AppConstants.INDEX_CUSTOMERS, CustomerInfo.class);
        return new ResponseEntity<>(allCustomers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerInfo> getCustomerById(@PathVariable String id) throws Exception {
        CustomerInfo customer = elasticsearchService.getById(AppConstants.INDEX_CUSTOMERS, id, CustomerInfo.class);
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerInfo> addOrderCustomer(@PathVariable String id, @RequestBody String orderId) {
        CustomerInfo updatedCustomer = customerInfoService.addOrderToCustomer(id, orderId);
        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomerById(@PathVariable String id) {
        boolean deleted = customerInfoService.deleteCustomerBuId(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
