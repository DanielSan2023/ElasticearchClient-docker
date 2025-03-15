package org.springboot.controller;

import org.springboot.exception.ProductNotFoundException;
import org.springboot.model.Product;
import org.springboot.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Iterable<Product>> getAllProducts() throws IOException {
        Iterable<Product> allProducts = productService.getAllProducts();
        return new ResponseEntity<>(allProducts, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) throws ProductNotFoundException {
        Product product = productService.getProductByID(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) throws ProductNotFoundException {
        Product updateProduct = productService.updateProduct(id, product);
        return new ResponseEntity<>(updateProduct, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) throws ProductNotFoundException {
        boolean deleted = productService.deleteProduct(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search/{category}")
    public ResponseEntity<List<Product>> getAllProductByCategory(@PathVariable String category) throws IOException {
        List<Product> products = productService.getProductByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/searchByPriceRange")
    public ResponseEntity<List<Product>> searchByPriceRange(@RequestParam double minPrice, @RequestParam double maxPrice) throws IOException {
        List<Product> products = productService.searchByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/fuzzy")
    public ResponseEntity<List<Product>> fuzzySearch(@RequestParam("query") String searchTerm) {
        List<Product> products = productService.fuzzySearch(searchTerm);
        return ResponseEntity.ok(products);
    }
}
