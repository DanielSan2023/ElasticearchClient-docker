package org.springboot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.springboot.exception.ProductNotFoundException;
import org.springboot.generator.MyUuidGenerator;
import org.springboot.model.Order;
import org.springboot.model.Product;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
        String orderId = MyUuidGenerator.generateUuid();
        List<Product> products = new ArrayList<>();

        try {
            for (String ean : productEans) {
                Product product = productService.getProductByEAN(ean);
                if (product != null) {
                    products.add(product);
                }
            }

            double totalAmount = products.stream().mapToDouble(Product::getPrice).sum();
            Order order = new Order(orderId, customerId, Instant.now(), totalAmount, products);

            saveOrder(order);

            return order;
        } catch (IOException e) {
            throw new RuntimeException("Error while processing the order: " + e.getMessage(), e);
        } catch (ProductNotFoundException e) {
            throw new RuntimeException(e);
        }
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
