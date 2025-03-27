package org.springboot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.springboot.generator.MyUuidGenerator;
import org.springboot.model.CustomerInfo;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CustomerInfoServiceImpl implements CustomerInfoService {

    private final ElasticsearchClient client;

    public CustomerInfoServiceImpl(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public CustomerInfo saveCustomer(CustomerInfo customer) {
        String customerId = MyUuidGenerator.generateUuid();
        customer.setCustomerId(customerId);

        try {
            GetRequest getRequest = new GetRequest.Builder()
                    .index("customers-002")
                    .id(customerId)
                    .build();
            GetResponse<CustomerInfo> newCustomer = client.get(getRequest, CustomerInfo.class);

            if (newCustomer.found()) {
                throw new IllegalStateException("Customer with id : " + customerId + " already exists in database.");
            }

            IndexResponse response = client.index(i -> i
                    .index("customers-002")
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
}
