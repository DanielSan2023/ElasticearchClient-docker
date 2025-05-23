package org.springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springboot.exception.ProductNotFoundException;
import org.springboot.model.Product;
import org.springboot.service.ElasticsearchServiceImpl;
import org.springboot.service.ProductService;
import org.springboot.utility.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ElasticsearchServiceImpl elasticsearchService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.addProduct(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.OK);
    }

    @PutMapping("/sold/{id}")
    public ResponseEntity<Product> soldProduct(@PathVariable String id) throws ProductNotFoundException {
        Product updateProduct = productService.soldProduct(id);
        return new ResponseEntity<>(updateProduct, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Iterable<Product>> getAllProducts() {
        Iterable<Product> allProducts = elasticsearchService.getAll(AppConstants.INDEX_PRODUCTS, Product.class);
        return new ResponseEntity<>(allProducts, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Product product = elasticsearchService.getById(AppConstants.INDEX_PRODUCTS, id, Product.class);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) throws ProductNotFoundException {
        Product updateProduct = productService.updateProduct(id, product);
        return new ResponseEntity<>(updateProduct, HttpStatus.OK);
    }

    @GetMapping("/search/{category}")
    public ResponseEntity<List<Product>> getAllProductByCategory(@PathVariable String category) throws ProductNotFoundException {
        List<Product> products = productService.getProductByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/searchByPriceRange")
    public ResponseEntity<List<Product>> searchByPriceRange(@RequestParam double minPrice, @RequestParam double maxPrice) throws ProductNotFoundException {
        List<Product> products = productService.searchByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/fuzzy")
    public ResponseEntity<List<Product>> fuzzySearch(@RequestParam("query") String searchTerm) throws ProductNotFoundException {
        List<Product> products = productService.fuzzySearch(searchTerm);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/productsByNgram")
    public ResponseEntity<List<Product>> getProductsByNgram(@RequestParam("query") String searchTerm) throws ProductNotFoundException {
        List<Product> products = productService.getProductsByNgram(searchTerm);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search/hybrid")
    public Mono<List<Product>> searchProducts(@RequestParam String query) {
        return productService.hybridSearch(query);
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
}
