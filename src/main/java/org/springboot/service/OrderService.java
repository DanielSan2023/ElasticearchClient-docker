package org.springboot.service;

import org.springboot.model.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {
    public Order addOrder(String customerId, List<String> productEans);
}
