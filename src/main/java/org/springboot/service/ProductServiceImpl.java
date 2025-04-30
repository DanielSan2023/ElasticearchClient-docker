package org.springboot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import org.springboot.exception.ProductNotFoundException;
import org.springboot.generator.EANGenerator;
import org.springboot.model.Product;
import org.springboot.utility.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    private final ElasticsearchClient client;
    private final ElasticsearchServiceImpl elasticsearchService;

    @Autowired
    public ProductServiceImpl(ElasticsearchClient client, ElasticsearchServiceImpl elasticsearchService) {
        this.client = client;
        this.elasticsearchService = elasticsearchService;
    }

    @Override
    public Product addProduct(Product product) {
        String eanCode = EANGenerator.generateRandomEAN13();
        product.setEan(eanCode);

        try {
            boolean indexExists = client.indices().exists(e -> e.index(AppConstants.INDEX_PRODUCTS)).value();

            if (!indexExists) {
                client.indices().create(c -> c.index(AppConstants.INDEX_PRODUCTS));
            }

            GetRequest getRequest = new GetRequest.Builder()
                    .index(AppConstants.INDEX_PRODUCTS)
                    .id(eanCode)
                    .build();
            GetResponse<Product> existingProduct = client.get(getRequest, Product.class);


            if (existingProduct.found()) {
                throw new IllegalStateException("Product with EAN: " + eanCode + " already exists.");
            }

            IndexResponse response = client.index(i -> i
                    .index(AppConstants.INDEX_PRODUCTS)
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
        Product product = Optional.ofNullable(elasticsearchService.getById(AppConstants.INDEX_PRODUCTS, ean, Product.class))
                .orElseThrow(() -> new ProductNotFoundException("Product with EAN: " + ean + " not found"));

        if (product.getAvailable() <= 0) {
            throw new IllegalStateException("No stock available for EAN: " + ean);
        }

        int newAvailable = product.getAvailable() - 1;
        int newSold = product.getSold() + 1;

        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("available", newAvailable);
        updateFields.put("sold", newSold);

        UpdateRequest<Product, Map<String, Object>> request = new UpdateRequest.Builder<Product, Map<String, Object>>()
                .index(AppConstants.INDEX_PRODUCTS)
                .id(ean)
                .doc(updateFields)
                .build();

        try {
            UpdateResponse<Product> response = client.update(request, Product.class);

            if (response.result() == Result.Updated) {
                product.setAvailable(newAvailable);
                product.setSold(newSold);
                return product;
            } else {
                throw new ProductNotFoundException("Product update failed: EAN " + ean + " not found in Elasticsearch");
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem with updating product with EAN: " + ean, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while updating product", e);
        }
    }

    @Override
    public Product getProductByEAN(String ean) throws ProductNotFoundException {
        GetRequest request = new GetRequest.Builder()
                .index(AppConstants.INDEX_PRODUCTS)
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
            throw new ProductNotFoundException("Problem with finding product with EAN: " + ean, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while searching for product with EAN: " + ean, e);
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
                .index(AppConstants.INDEX_PRODUCTS)
                .id(ean)
                .doc(updateFields)
                .build();

        try {
            UpdateResponse<Product> response = client.update(request, Product.class);

            if (response.result() == Result.Updated) {
                return product;
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
                .index(AppConstants.INDEX_PRODUCTS)
                .id(id)
                .build();

        DeleteResponse response;
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
                .index(AppConstants.INDEX_PRODUCTS)
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
                .index(AppConstants.INDEX_PRODUCTS)
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
                .index(AppConstants.INDEX_PRODUCTS)
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
                .index(AppConstants.INDEX_PRODUCTS)
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

    @Override
    public void updateProductsAfterOrderDeletion(List<Product> products) {
        for (Product product : products) {
            String ean = product.getEan();
            int newAvailable = product.getAvailable() + 1;
            int newSold = Math.max(0, product.getSold() - 1);

            product.setAvailable(newAvailable);
            product.setSold(newSold);

            try {
                updateProduct(ean, product);
            } catch (ProductNotFoundException e) {
                throw new RuntimeException("Product with EAN " + ean + " not found", e);
            }
        }
    }
}
