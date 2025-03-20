package org.springboot.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Product {

    private String ean;

    private String name;

    private String description;

    private double price;

    private String category;

    private int available;

    private int sold;

    private List<Instant> soldTimestamps = new ArrayList<>();

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public List<Instant> getSoldTimestamps() {
        return soldTimestamps;
    }

    public void setSoldTimestamps(List<Instant> soldTimestamps) {
        this.soldTimestamps = soldTimestamps;
    }

    @Override
    public String toString() {
        return "Product{" +
                "ean='" + ean + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", available=" + available +
                ", sold=" + sold +
                ", soldTimestamps=" + soldTimestamps +
                '}';
    }
}


