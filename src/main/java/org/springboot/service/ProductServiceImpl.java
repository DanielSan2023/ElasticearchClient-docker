package org.springboot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import org.springboot.exception.ProductNotFoundException;
import org.springboot.generator.EANGenerator;
import org.springboot.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    ElasticsearchClient client;

    @Autowired
    public ProductServiceImpl(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public Product addProduct(Product product) {
        String eanCode = EANGenerator.generateRandomEAN13();
        product.setEan(eanCode);

        try {
            GetRequest getRequest = new GetRequest.Builder()
                    .index("products-002")
                    .id(eanCode)
                    .build();
            GetResponse<Product> existingProduct = client.get(getRequest, Product.class);

            if (existingProduct.found()) {
                throw new IllegalStateException("Product with EAN: " + eanCode + " already exists.");
            }

            IndexResponse response = client.index(i -> i
                    .index("products-002")
                    .id(eanCode)
                    .document(product));

            if (response.result() == Result.Created) {
                return product;
            } else {
                throw new RuntimeException("Failed to add product with EAN: " + eanCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem with adding product with EAN: " + eanCode, e);
        }
    }

    @Override
    public Product soldProduct(String ean) throws ProductNotFoundException {
        Product product = Optional.ofNullable(getProductByEAN(ean))
                .orElseThrow(() -> new ProductNotFoundException("Product with EAN: " + ean + " not found"));

        if (product.getAvailable() <= 0) {
            throw new IllegalStateException("No stock available for EAN: " + ean);
        }

        int newAvailable = product.getAvailable() - 1;
        int newSold = product.getSold() + 1;
        Instant timestamp = Instant.now();

        List<Instant> updatedSoldTimestamps = new ArrayList<>(product.getSoldTimestamps());
        updatedSoldTimestamps.add(timestamp);

        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("available", newAvailable);
        updateFields.put("sold", newSold);
        updateFields.put("soldTimestamps", updatedSoldTimestamps);

        UpdateRequest<Product, Map<String, Object>> request = new UpdateRequest.Builder<Product, Map<String, Object>>()
                .index("products-002")
                .id(ean)
                .doc(updateFields)
                .build();

        try {
                        System.out.println("Sending update request for product with EAN: " + ean);
            UpdateResponse<Product> response = client.update(request, Product.class);

                        System.out.println("Response from Elasticsearch: " + response);

            if (response.result() == Result.Updated) {
                product.setAvailable(newAvailable);
                product.setSold(newSold);
                product.setSoldTimestamps(updatedSoldTimestamps);
                return product;
            } else {
                throw new ProductNotFoundException("Product update failed: EAN " + ean + " not found in Elasticsearch");
            }
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            e.printStackTrace();  // Logs the full stack trace for better debugging
            throw new RuntimeException("Problem with updating product with EAN: " + ean, e);
        } catch (IllegalStateException e) {
            System.err.println("IllegalStateException occurred: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error occurred while updating product", e);
        }
    }

    @Override
    public Iterable<Product> getAllProducts() throws IOException {
        SearchRequest request = new SearchRequest.Builder()
                .index("products-002")
                .size(100)
                .build();
        SearchResponse response = client.search(request, Product.class);

        List<Hit<Product>> hits = response.hits().hits();

        List<Product> products = new ArrayList<>();
        for (Hit<Product> hit : hits) {
            Product product = hit.source();
            products.add(product);
        }
        return products;
    }

@Override
public Product getProductByEAN(String ean) throws ProductNotFoundException {
    GetRequest request = new GetRequest.Builder()
            .index("products-002")
            .id(ean)
            .build();

    try {
        GetResponse<Product> response = client.get(request, Product.class);
        if (response.found()) {
            return response.source();
        } else {
            throw new ProductNotFoundException("Product with ID: " + ean + " not found");
        }
    } catch (IOException e) {
        throw new ProductNotFoundException("Problem with find product with ID: " + ean, e);
    }
}

@Override
public Product updateProduct(String ean, Product product) throws ProductNotFoundException {
    Product existingProduct = getProductByEAN(ean);

    if (existingProduct == null) {
        throw new ProductNotFoundException("Product with ID: " + ean + " not found");
    }

    Map<String, Object> updateFields = Map.of(
            "name", product.getName(),
            "description", product.getDescription(),
            "price", product.getPrice(),
            "category", product.getCategory(),
            "available", product.getAvailable(),
            "sold", product.getSold()
    );
    UpdateRequest<Product, Map<String, Object>> request = new UpdateRequest.Builder<Product, Map<String, Object>>()
            .index("products-002")
            .id(ean)
            .doc(updateFields)
            .build();

    try {
        UpdateResponse<Product> response = client.update(request, Product.class);

        if (response.result() == Result.Updated) {
            return product; // TODO check if get updated product from Elasticsearch
        } else {
            throw new ProductNotFoundException("Product update failed: ID " + ean + " not found");
        }
    } catch (IOException e) {
        throw new ProductNotFoundException("Problem with updating product with ID: " + ean, e);
    }
}

@Override
public boolean deleteProduct(String id) throws ProductNotFoundException {
    DeleteRequest request = new DeleteRequest.Builder()
            .index("products-002")
            .id(id)
            .build();

    DeleteResponse response = null;
    try {
        response = client.delete(request);
        if (response.result() == Result.Deleted) {
            return true;
        } else {
            throw new ProductNotFoundException("Product deletion failed: ID " + id + " not found");
        }
    } catch (IOException e) {
        throw new ProductNotFoundException("Problem with deleting product with ID: " + id, e);
    }
}

@Override
public List<Product> getProductByCategory(String category) throws ProductNotFoundException {
    SearchRequest request = new SearchRequest.Builder()
            .index("products-002")
            .query(q -> q
                    .match(t -> t
                            .field("category")
                            .query(category)
                            .fuzziness("AUTO")))
            .build();

    try {
        return getProductListFromResponse(request);
    } catch (IOException e) {
        throw new ProductNotFoundException("Error while fetching products by category: " + category, e);
    }
}

@Override
public List<Product> searchByPriceRange(double minPrice, double maxPrice) throws ProductNotFoundException {
    SearchRequest request = new SearchRequest.Builder()
            .index("products-002")
            .query(q -> q
                    .range(r -> r
                            .field("price")
                            .gte(JsonData.of(minPrice))
                            .lte(JsonData.of(maxPrice))))
            .build();

    try {
        return getProductListFromResponse(request);
    } catch (IOException e) {
        throw new ProductNotFoundException("Error while fetching products in the price range: " + minPrice + " to " + maxPrice, e);
    }
}

@Override
public List<Product> fuzzySearch(String searchTerm) throws ProductNotFoundException {
    SearchRequest request = new SearchRequest.Builder()
            .index("products-002")
            .query(q -> q
                    .bool(b -> b
                            .should(s -> s
                                    .fuzzy(f -> f
                                            .field("category")
                                            .value(searchTerm)
                                            .fuzziness("AUTO")))
                            .should(s -> s
                                    .fuzzy(f -> f
                                            .field("name")
                                            .value(searchTerm)
                                            .fuzziness("AUTO")))))
            .build();

    try {
        return getProductListFromResponse(request);
    } catch (IOException e) {
        throw new ProductNotFoundException("Error while fetching products for search term: " + searchTerm, e);
    }
}

@Override
public List<Product> getProductsByNgram(String searchTerm) throws ProductNotFoundException {
    SearchRequest request = new SearchRequest.Builder()
            .index("products-002")
            .query(q -> q
                    .prefix(p -> p
                            .field("name")
                            .value(searchTerm)))
            .build();

    try {
        return getProductListFromResponse(request);
    } catch (IOException e) {
        throw new ProductNotFoundException("Error while fetching products for search term: " + searchTerm, e);
    }
}

private List<Product> getProductListFromResponse(SearchRequest request) throws IOException {
    SearchResponse<Product> response = null;

    response = client.search(request, Product.class);
    List<Hit<Product>> hits = response.hits().hits();
    List<Product> products = new ArrayList<>();
    for (Hit<Product> hit : hits) {
        Product product = hit.source();
        products.add(product);
    }
    return products;
}
}
