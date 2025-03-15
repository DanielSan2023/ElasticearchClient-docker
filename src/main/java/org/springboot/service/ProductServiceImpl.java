package org.springboot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import org.springboot.exception.ProductNotFoundException;
import org.springboot.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ElasticsearchClient client;

    @Override
    public Product createProduct(Product product) {
        try {
            IndexResponse response = client.index(i -> i
                    .index("products-002")
                    .id(product.getId())
                    .document(product));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Iterable<Product> getAllProducts() throws IOException {
        SearchRequest request = new SearchRequest.Builder()
                .index("products-002")
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
    public Product getProductByID(String id) throws ProductNotFoundException {
        GetRequest request = new GetRequest.Builder()
                .index("products-002")
                .id(id)
                .build();

        try {
            GetResponse<Product> response = client.get(request, Product.class);
            if (response.found()) {
                return response.source();
            } else {
                throw new ProductNotFoundException("Product with ID: " + id + " not found");
            }
        } catch (IOException e) {
            throw new ProductNotFoundException("Problem with find product with ID: " + id, e);
        }
    }

    @Override
    public Product updateProduct(String id, Product product) throws ProductNotFoundException {
        UpdateRequest request = new UpdateRequest.Builder<Product, Product>()
                .index("products-002")
                .id(id)
                .doc(product)
                .build();

        UpdateResponse response = null;
        try {
            response = client.update(request, Product.class);

            if (response.result() == Result.Updated) {
                return product; // TODO check if get updated product from Elasticsearch
            } else {
                throw new ProductNotFoundException("Product update failed: ID " + id + " not found");
            }
        } catch (IOException e) {
            throw new ProductNotFoundException("Problem with updating product with ID: " + id, e);
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
            throw new RuntimeException("Problem with deleting product with ID: " + id, e);
        }
    }

    @Override
    public List<Product> getProductByCategory(String category) {
        SearchRequest request = new SearchRequest.Builder()
                .index("products-002")
                .query(q -> q
                        .match(t -> t
                                .field("category")
                                .query(category)))
                .build();


        SearchResponse response = null;
        try {
            response = client.search(request, Product.class);

            List<Hit<Product>> hits = response.hits().hits();
            List<Product> products = new ArrayList<>();
            for (Hit<Product> hit : hits) {
                Product product = hit.source();
                products.add(product);
            }
            return products;
        } catch (IOException e) {
            throw new RuntimeException("Error while fetching products by category: " + category, e);
        }
    }

    @Override
    public List<Product> searchByPriceRange(double minPrice, double maxPrice) {
        SearchRequest request = new SearchRequest.Builder()
                .index("products-002")
                .query(q -> q
                        .range(r -> r
                                .field("price")
                                .gte(JsonData.of(minPrice))
                                .lte(JsonData.of(maxPrice))))
                .build();

        try {
            SearchResponse response = client.search(request, Product.class);

            List<Hit<Product>> hits = response.hits().hits();
            List<Product> products = new ArrayList<>();

            for (Hit<Product> hit : hits) {
                Product product = hit.source();
                products.add(product);
            }
            return products;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Product> fuzzySearch(String searchTerm) {
        SearchRequest request = new SearchRequest.Builder()
                .index("products-002")
                .query(q -> q
                        .bool(b -> b
                                .should(s -> s
                                        .fuzzy(f -> f
                                                .field("category")
                                                .value(searchTerm)
                                                .fuzziness("AUTO"))
                                )
                                .should(s -> s
                                        .fuzzy(f -> f
                                                .field("name")
                                                .value(searchTerm)
                                                .fuzziness("AUTO"))
                                )
                        )
                )
                .build();

        SearchResponse<Product> response = null;
        try {
            response = client.search(request, Product.class);

            List<Hit<Product>> hits = response.hits().hits();
            List<Product> products = new ArrayList<>();
            for (Hit<Product> hit : hits) {
                Product product = hit.source();
                products.add(product);
            }
            return products;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
