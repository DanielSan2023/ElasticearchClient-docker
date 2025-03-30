package org.springboot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springboot.dto.OrderDto;
import org.springboot.exception.ProductNotFoundException;
import org.springboot.generator.MyUuidGenerator;
import org.springboot.model.Order;
import org.springboot.model.Product;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    ElasticsearchClient client;
    ProductServiceImpl productService;

    public OrderServiceImpl(ElasticsearchClient client, ProductServiceImpl productService) {
        this.client = client;
        this.productService = productService;
    }

    @Override
    public Order addOrder(String customerId, List<String> productEans) {
        try {
            double totalAmount = productEans.stream()
                    .map(ean -> {
                        try {
                            return productService.soldProduct(ean);
                        } catch (ProductNotFoundException e) {
                            throw new RuntimeException("Product with EAN " + ean + " not found", e);
                        }
                    })
                    .mapToDouble(Product::getPrice)
                    .sum();

            String orderId = MyUuidGenerator.generateUuid();
            Order order = new Order(orderId, customerId, totalAmount, productEans);

            saveOrder(order);
            return order;

        } catch (IOException e) {
            throw new RuntimeException("Error while processing the order for customerId: " + customerId, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while processing the order", e);
        }
    }

    @Override
    public Iterable<Order> getAllOrders() {
        SearchRequest request = new SearchRequest.Builder()
                .index("orders-002")
                .size(100)
                .build();
        try {
            SearchResponse response = client.search(request, Order.class);
            System.out.println(response);

            List<Hit<Order>> hits = response.hits().hits();
            List<Order> orders = new ArrayList<>();
            for (Hit<Order> hit : hits) {
                Order order = hit.source();
                orders.add(order);
            }
            return orders;
        } catch (IOException e) {
            throw new RuntimeException("Error while processing the order: " + e.getMessage(), e);
        }
    }

    @Override
    public Order getById(String id) {
        GetRequest request = new GetRequest.Builder()
                .index("orders-002")
                .id(id)
                .build();

        try {
            GetResponse<Order> response = client.get(request, Order.class);
            if (response.found()) {
                return response.source();
            } else {
                throw new RuntimeException("Order with ID: " + id + " not found");
            }
        } catch (IOException e) {
            throw new RuntimeException("Order with find product with ID: " + id, e);
        }
    }

    public OrderDto getOrderWithProducts(String orderId) {
        Order order = getById(orderId);

        List<Product> products = order.getProductEans().stream()
                .map(ean -> {
                    try {
                        return productService.soldProduct(ean);
                    } catch (ProductNotFoundException e) {
                        throw new RuntimeException("Product with EAN " + ean + " not found", e);
                    }
                })
                .collect(Collectors.toList());

        return new OrderDto(order,products);
    }

    private void saveOrder(Order order) throws IOException {
        IndexRequest<Order> request = IndexRequest.of(i -> i
                .index("orders-002")
                .id(order.getOrderId())
                .document(order)
        );

        IndexResponse response = client.index(request);
        if (!response.result().name().equals("Created")) {
            throw new RuntimeException("Failed to save order in Elasticsearch");
        }
    }
}
