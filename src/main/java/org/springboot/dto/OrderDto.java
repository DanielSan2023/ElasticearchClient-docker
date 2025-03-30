package org.springboot.dto;

import org.springboot.model.Order;
import org.springboot.model.Product;

import java.util.List;

public record OrderDto(Order order, List<Product> products) {
}
