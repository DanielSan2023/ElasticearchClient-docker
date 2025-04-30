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

    OrderDto getOrderWithProducts(String orderId);

    boolean deleteOrderById(String id);
}
