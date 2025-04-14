package org.springboot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.*;
import org.springboot.exception.ProductNotFoundException;
import org.springboot.generator.MyUuidGenerator;
import org.springboot.model.CustomerInfo;
import org.springboot.utility.AppConstants;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;

@Service
public class CustomerInfoServiceImpl implements CustomerInfoService {

    private final ElasticsearchClient client;
    private final ElasticsearchServiceImpl elasticsearchService;

    public CustomerInfoServiceImpl(ElasticsearchClient client, ElasticsearchServiceImpl elasticsearchService) {
        this.client = client;
        this.elasticsearchService = elasticsearchService;
    }

    @Override
    public CustomerInfo saveCustomer(CustomerInfo customer) {
        String customerId = MyUuidGenerator.generateUuid();
        customer.setCustomerId(customerId);

        try {
            boolean indexExists = client.indices().exists(e -> e.index(AppConstants.INDEX_CUSTOMERS)).value();

            if (!indexExists) {
                client.indices().create(c -> c.index(AppConstants.INDEX_CUSTOMERS));
            }
            GetRequest getRequest = new GetRequest.Builder()
                    .index(AppConstants.INDEX_CUSTOMERS)
                    .id(customerId)
                    .build();
            GetResponse<CustomerInfo> newCustomer = client.get(getRequest, CustomerInfo.class);

            if (newCustomer.found()) {
                throw new IllegalStateException("Customer with id : " + customerId + " already exists in database.");
            }

            IndexResponse response = client.index(i -> i
                    .index(AppConstants.INDEX_CUSTOMERS)
                    .id(customerId)
                    .document(customer));

            if (response.result() == Result.Created) {
                return customer;
            } else {
                throw new RuntimeException("Failed to add customer with id: " + customerId);
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem with adding customer with id: " + customerId, e);
        }
    }

    @Override
    public CustomerInfo addOrderToCustomer(String customerId, String orderId) {
        try {
            CustomerInfo customer = elasticsearchService.getById(AppConstants.INDEX_CUSTOMERS, customerId, CustomerInfo.class);

            List<String> orders = Optional.ofNullable(customer.getOrderIds()).orElseGet(ArrayList::new);
            orders.add(orderId);
            customer.setOrderIds(orders);

            Map<String, Object> updateFields = new HashMap<>();
            updateFields.put("orderIds", orders);

            UpdateRequest<CustomerInfo, Map<String, Object>> request = new UpdateRequest.Builder<CustomerInfo, Map<String, Object>>()
                    .index(AppConstants.INDEX_CUSTOMERS)
                    .id(customerId)
                    .doc(updateFields)
                    .build();

            UpdateResponse<CustomerInfo> response = client.update(request, CustomerInfo.class);

            if (response.result().name().equalsIgnoreCase("noop")) {
                throw new RuntimeException("No update was performed for customer ID: " + customerId);
            }
            return customer;
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Customer with Id: " + customerId + " not found in Elastic!");
        } catch (IOException e) {
            throw new RuntimeException("Error while updating customer orders for customerId: " + customerId, e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while updating customer orders", e);
        }
    }

    @Override
    public boolean deleteCustomerBuId(String id) {
        DeleteRequest request = new DeleteRequest.Builder()
                .index(AppConstants.INDEX_CUSTOMERS)
                .id(id)
                .build();

        DeleteResponse response;
        try {
            response = client.delete(request);
            if (response.result() == Result.Deleted) {
                return true;
            } else {
                throw new NoSuchElementException("Customer deletion failed: ID " + id + " not found");
            }
        } catch (IOException e) {
            throw new NoSuchElementException("Problem with deleting customer with ID: " + id, e);
        }
    }
}
