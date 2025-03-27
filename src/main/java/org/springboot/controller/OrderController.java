package org.springboot.controller;

import org.springboot.model.Order;
import org.springboot.model.Product;
import org.springboot.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @PostMapping
    public ResponseEntity<Order> getOrderByCustomer(@RequestParam String customerId, @RequestParam List<String> productEans) {
        Order order = orderService.addOrder(customerId, productEans);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

}
