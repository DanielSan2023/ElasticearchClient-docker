package org.springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {

    private String orderId;
    private String customerId;
    private Long orderDate;
    private double totalAmount;
    private List<String> productEans;

    public Order(String orderId, String customerId, double totalAmount, List<String> productEans) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = Instant.now().toEpochMilli();
        this.totalAmount = totalAmount;
        this.productEans = productEans;
    }

    public Order() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFormattedOrderDate() {
        if (orderDate == null) return null;
        return DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(Instant.ofEpochMilli(orderDate));
    }

    public Long getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Long orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<String> getProductEans() {
        return productEans;
    }

    public void setProductEans(List<String> productEans) {
        this.productEans = productEans;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", orderDate=" + getFormattedOrderDate() +
                ", totalAmount=" + totalAmount +
                ", productEans=" + productEans +
                '}';
    }
}
