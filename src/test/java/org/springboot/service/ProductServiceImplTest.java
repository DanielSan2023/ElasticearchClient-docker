package org.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springboot.controller.CustomerInfoController;
import org.springboot.controller.OrderController;
import org.springboot.controller.ProductController;
import org.springboot.model.CustomerInfo;
import org.springboot.model.Order;
import org.springboot.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Profile("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@Import(ElasticsearchTestConfig.class)
@Testcontainers
public class ProductServiceImplTest {

    @Autowired
    private ProductController productController;

    @Autowired
    private CustomerInfoController customerController;

    @Autowired
    private OrderController orderController;

    @Container
    static final ElasticsearchContainer elasticsearchContainer = new ProductElasticsearchContainer();

    @DynamicPropertySource
    static void elasticsearchProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        String hostAddress = elasticsearchContainer.getHttpHostAddress();
        System.out.println("Elasticsearch is running at: " + hostAddress);
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
    }

    @Test
    public void GIVEN_customer_product_WHEN_create_order_THEN_retrieve_order_verify_product_available() throws Exception {
        // GIVEN product
        Product product = new Product();
        product.setName("NewProduct");
        product.setDescription("NewProductReadyForSold");
        product.setPrice(50.0);
        product.setCategory("Clothing");
        product.setAvailable(5);
        product.setSold(0);

        productController.createProduct(product);

        Product fetchedProduct = productController.getProductById(product.getEan()).getBody();
        assertNotNull(fetchedProduct);

        // GIVEN customer
        CustomerInfo customer = new CustomerInfo();
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setEmail("jane.smith@example.com");

        CustomerInfo createdCustomer = customerController.createCustomerInfo(customer).getBody();
        assertNotNull(createdCustomer);

        CustomerInfo fetchedCustomer = customerController.getCustomerById(createdCustomer.getCustomerId()).getBody();
        assertNotNull(fetchedCustomer);

        // WHEN order creation
        List<String> fetchedProductEan = List.of(fetchedProduct.getEan());
        Order createdOrder = orderController.addOrderByCustomerAndProduct(fetchedCustomer.getCustomerId(), fetchedProductEan).getBody();
        assertNotNull(createdOrder);

        // WHEN retrieving the order
        Order addedOrder = orderController.getOrderById(createdOrder.getOrderId()).getBody();
        assertNotNull(addedOrder);

        // THEN assert order content
        assertEquals(createdCustomer.getCustomerId(), addedOrder.getCustomerId());
        assertEquals(1, addedOrder.getProductEans().size());
        assertEquals(fetchedProduct.getEan(), addedOrder.getProductEans().get(0));
        assertEquals(50.0, addedOrder.getTotalAmount());

        // THEN verify product sold/available values updated
        Product updatedProduct = productController.getProductById(fetchedProduct.getEan()).getBody();
        assertNotNull(updatedProduct);
        assertEquals(4, updatedProduct.getAvailable());
        assertEquals(1, updatedProduct.getSold());
    }
}