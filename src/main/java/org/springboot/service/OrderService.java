package org.springboot.service;

import org.springboot.dto.OrderDto;
import org.springboot.exception.ProductNotFoundException;
import org.springboot.model.Order;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface OrderService {
    Order addOrder(String customerId, List<String> productEans) throws ProductNotFoundException;

    Iterable<Order> getAllOrders() throws IOException;

    Order getById(String id) throws IOException;

     OrderDto getOrderWithProducts(String orderId);
}
