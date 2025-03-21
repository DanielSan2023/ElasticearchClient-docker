package org.springboot.model;

import java.time.Instant;
import java.util.List;

public class Order {

    private String orderId;
    private String customerId;
    private Instant orderDate;
    private double totalAmount;

    private List<Product> products;

}
