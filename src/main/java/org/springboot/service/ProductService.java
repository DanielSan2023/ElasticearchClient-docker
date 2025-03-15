package org.springboot.service;

import org.springboot.exception.ProductNotFoundException;
import org.springboot.model.Product;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface ProductService {
    Product createProduct(Product product);

    Iterable<Product> getAllProducts() throws IOException;

    Product getProductByID(String id) throws ProductNotFoundException;

    Product updateProduct(String id, Product product) throws ProductNotFoundException;


    boolean deleteProduct(String id) throws ProductNotFoundException;

    List<Product> getProductByCategory(String category);

    List<Product> searchByPriceRange(double minPrice, double maxPrice);

    List<Product> fuzzySearch(String searchTerm);
}
