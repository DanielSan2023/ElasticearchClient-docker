package org.springboot.service;

import org.springboot.exception.ProductNotFoundException;
import org.springboot.model.Product;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface ProductService {
    Product addProduct(Product product);

    Iterable<Product> getAllProducts() throws IOException;

    Product getProductByEAN(String id) throws ProductNotFoundException;

    Product updateProduct(String id, Product product) throws ProductNotFoundException;


    boolean deleteProduct(String id) throws ProductNotFoundException;

    List<Product> getProductByCategory(String category) throws ProductNotFoundException;

    List<Product> searchByPriceRange(double minPrice, double maxPrice) throws ProductNotFoundException;

    List<Product> fuzzySearch(String searchTerm) throws ProductNotFoundException;

    List<Product> getProductsByNgram(String searchTerm) throws ProductNotFoundException;


}
