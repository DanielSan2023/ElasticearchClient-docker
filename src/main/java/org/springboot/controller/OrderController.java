package org.springboot.controller;

import org.springboot.dto.OrderDto;
import org.springboot.exception.ProductNotFoundException;
import org.springboot.model.Order;
import org.springboot.service.ElasticsearchServiceImpl;
import org.springboot.service.OrderService;
import org.springboot.utility.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final ElasticsearchServiceImpl elasticsearchService;

    public OrderController(OrderService orderService, ElasticsearchServiceImpl elasticsearchService) {
        this.orderService = orderService;
        this.elasticsearchService = elasticsearchService;
    }

    @PostMapping
    public ResponseEntity<Order> addOrderByCustomerAndProduct(@RequestParam String customerId, @RequestParam List<String> productEans) throws ProductNotFoundException {
        Order order = orderService.addOrder(customerId, productEans);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = elasticsearchService.getAll(AppConstants.INDEX_ORDERS, Order.class);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        Order order = elasticsearchService.getById(AppConstants.INDEX_ORDERS, id, Order.class);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @GetMapping("/productsInfo/{id}")
    public ResponseEntity<OrderDto> getOrderByIdWithProducts(@PathVariable String id) {
        OrderDto order = orderService.getOrderWithProducts(id);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrderById(@PathVariable String id) {
        boolean isDeleted = orderService.deleteOrderById(id);

        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
