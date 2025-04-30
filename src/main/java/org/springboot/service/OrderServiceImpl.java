package org.springboot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.*;
import org.springboot.dto.OrderDto;
import org.springboot.exception.ProductNotFoundException;
import org.springboot.generator.MyUuidGenerator;
import org.springboot.model.CustomerInfo;
import org.springboot.model.Order;
import org.springboot.model.Product;
import org.springboot.utility.AppConstants;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final ElasticsearchClient client;
    private final ProductServiceImpl productService;
    private final ElasticsearchServiceImpl elasticsearchService;
    private final CustomerInfoServiceImpl customerService;

    public OrderServiceImpl(ElasticsearchClient client, ProductServiceImpl productService, ElasticsearchServiceImpl elasticsearchService, CustomerInfoServiceImpl customerService) {
        this.client = client;
        this.productService = productService;
        this.elasticsearchService = elasticsearchService;
        this.customerService = customerService;
    }

    @Override
    public Order addOrder(String customerId, List<String> productEans) {
        validateCustomer(customerId);
        double totalAmount = getTotalAmount(productEans);

        try {
            String orderId = MyUuidGenerator.generateUuid();
            Order order = new Order(orderId, customerId, totalAmount, productEans);

            customerService.addOrderToCustomer(customerId, orderId);
            saveOrder(order);

            return order;
        } catch (IOException e) {
            throw new RuntimeException("Error while processing the order for customerId: " + customerId, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while processing the order", e);
        }
    }

    private void validateCustomer(String customerId) {
        Optional.ofNullable(elasticsearchService.getById(AppConstants.INDEX_CUSTOMERS, customerId, CustomerInfo.class))
                .orElseThrow(() -> new NoSuchElementException("Customer with ID: " + customerId + " not found"));
    }

    private double getTotalAmount(List<String> productEans) {
        return productEans.stream()
                .map(ean -> {
                    try {
                        return productService.soldProduct(ean);
                    } catch (ProductNotFoundException e) {
                        throw new RuntimeException("Product with EAN " + ean + " not found", e);
                    }
                })
                .mapToDouble(Product::getPrice)
                .sum();
    }

    public OrderDto getOrderWithProducts(String orderId) {
        Order order = elasticsearchService.getById(AppConstants.INDEX_ORDERS, orderId, Order.class);

        List<Product> products = order.getProductEans().stream()
                .map(ean -> {
                    try {
                        return productService.getProductByEAN(ean);
                    } catch (ProductNotFoundException e) {
                        throw new RuntimeException("Product with EAN " + ean + " not found", e);
                    }
                })
                .collect(Collectors.toList());

        return new OrderDto(order, products);
    }

    private void saveOrder(Order order) throws IOException {
        IndexRequest<Order> request = IndexRequest.of(i -> i
                .index(AppConstants.INDEX_ORDERS)
                .id(order.getOrderId())
                .document(order)
        );

        IndexResponse response = client.index(request);
        if (!response.result().name().equals("Created")) {
            throw new RuntimeException("Failed to save order in Elasticsearch");
        }
    }

    @Override
    public boolean deleteOrderById(String id) {
        GetRequest getRequest = new GetRequest.Builder()
                .index(AppConstants.INDEX_ORDERS)
                .id(id)
                .build();

        try {
            GetResponse<Order> getResponse = client.get(getRequest, Order.class);
            if (!getResponse.found()) {
                return false;
            }

            OrderDto order = getOrderWithProducts(id);
            List<Product> products = order.products();

            productService.updateProductsAfterOrderDeletion(products);

            DeleteRequest deleteRequest = new DeleteRequest.Builder()
                    .index(AppConstants.INDEX_ORDERS)
                    .id(id)
                    .build();

            DeleteResponse deleteResponse = client.delete(deleteRequest);

            return deleteResponse.result() == Result.Deleted;

        } catch (IOException e) {
            throw new NoSuchElementException("Problem with deleting order with ID: " + id, e);
        }
    }

}
