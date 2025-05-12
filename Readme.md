# Spring Boot Application with Elasticsearch & Qdrant Integration

## Overview

The **Elasticsearch Integration with Spring Boot** project demonstrates how to integrate both **Elasticsearch** and the **Qdrant vector database** into a Spring Boot application. It provides a scalable and efficient way to store, search, and manage customer, product, and order data.

The application uses:
- **Elasticsearch** for full-text search and structured data queries.
- **Qdrant** for semantic vector similarity search using product embeddings generated from names and descriptions.

The project leverages **Spring Data Elasticsearch** and the **Elasticsearch Java client** for seamless integration.
---

## Features

- **Elasticsearch Integration**: Uses Spring Data Elasticsearch for CRUD operations and advanced querying.
- **RESTful API**: Supports querying employee data using custom Elasticsearch queries.
- **Pagination**: Supports paginated results for large datasets.
- **Secure Connection**: Configures HTTPS and authentication for Elasticsearch.
- **JWT Authentication**: Implements JWT authentication for secure access to the API.
- **Qdrant Integration**: Uses Qdrant for vector similarity search and embedding storage.
    - **Vector Search**: Supports vector similarity search using Qdrant.
        - **Embedding Generation**: Generates embeddings for product descriptions and names.

---

## Project Structure

### Main Components:

1. **Entity - `Customer,Product,Order`**
    - Represent domain models stored in their respective Elasticsearch indices.
    - Annotated with Spring Data Elasticsearch annotations for indexing and querying..
    - Each entity maps to a document in Elasticsearch:
Customer → Customer index
Product → Product index
Order → Order index
   
2. **Service - `Service `**
    - Contains business logic for operations on customers, products, and orders.
    - Interfaces with repositories for Elasticsearch access.
    - Includes embedding and vector search logic for integration with Qdrant.
    - Supports pagination, filtering, and hybrid search features.

3. **Configuration - `GetESClient and HttpClientConfigImpl`**
    - Configures the Elasticsearch client with HTTPS and authentication.
    - Sets up the connection to the Elasticsearch instance.
    - Ensures the application communicates with Elasticsearch over HTTPS securely.

---

## Elasticsearch Index Design

### Index Schema Product

| Attribute   | Type   | Notes                                                             |
|-------------|--------|-------------------------------------------------------------------|
| `ean`       | String | Primary key. Unique product identifier (European Article Number). |
| `name`      | String | Index. Allows searching by product name.                          |
| `category`  | String | Index. Enables filtering by category.                             |
| `price`     | double | Index. Useful for sorting/filtering products by price.            |
| `available` | int    | Optional. Can be indexed for availability-based queries.          |
| `sold`      | int    | Optional. Index if you query top-selling products.                |

### Index Schema CustomerInfo

| Attribute    | Type         | Notes                                                     |
|--------------|--------------|-----------------------------------------------------------|
| `customerId` | String       | Primary key. Unique identifier for a customer.            |
| `email`      | String       | Unique index. Used to identify users during login.        |
| `role`       | String       | Index. For filtering by customer role (e.g., admin/user). |
| `orderIds`   | List<String> | Optional. Could use array indexing if supported.          |

### Index Schema Order

| Attribute     | Type         | Notes                                                       |
|---------------|--------------|-------------------------------------------------------------|
| `orderId`     | String       | Primary key. Unique identifier for an order.                |
| `customerId`  | String       | Index. Allows quick lookup of orders by customer.           |
| `orderDate`   | Long         | Index. Helps with date range queries (e.g., recent orders). |
| `totalAmount` | double       | Index. Useful for sorting/filtering based on order value.   |
| `productEans` | List<String> | Optional. Could be indexed for reverse lookup if needed.    |

### ProductController Methods - Endpoints("/api/products")

- **Add product**: `ResponseEntity<Product> createProduct(@RequestBody Product product);` `POST /api/products`
    - Description: `Adds a new product to the system.`

- **Sold product** `ResponseEntity<Product> soldProduct(@PathVariable String id);` `PUT /api/products/sold/{id}`
    - Description:  `Marks a product as sold by incrementing the sold count.`.

- **Get all products** `ResponseEntity<Iterable<Product>> getAllProducts();``GET /api/products `
    - Description:  `Retrieves all products from Elasticsearch product index.`.

- **Get product by Id** `ResponseEntity<Product> getProductById(@PathVariable String id);` `GET /api/products/{id}`
    - Description:  `Retrieves  products from Elasticsearch product index by Id - ean.`.

- **Get all product by Category** `ResponseEntity<List<Product>> getAllProductByCategory(@PathVariable String category);` `GET /api/products/search/{category}`
    - Description:  `Retrieves products by category.`.

- **Find products by Price Range** `ResponseEntity<List<Product>> searchByPriceRange(@RequestParam double minPrice, @RequestParam double maxPrice);` `GET /api/products/searchByPriceRange?minPrice=100&maxPrice=500`
    - Description:  `Retrieves products within a specified price range, min and max`.

- **Fuzzy search** `ResponseEntity<List<Product>> fuzzySearch(@RequestParam("query") String searchTerm);` `GET /api/products/search/fuzzy?query=term`
    - Description:  `Performs a fuzzy search on product fields.`.

- **N-gram search** `ResponseEntity<List<Product>> getProductsByNgram(@RequestParam("query") String searchTerm) ;` `GET /api/products/search/productsByNgram?query=term`
    - Description:  `Retrieves products using an N-gram-based search.`.

- **Hybrid search** `Mono<List<Product>> searchProducts(@RequestParam String query) ;`
    - Description:  `Executes a combined search strategy for more relevant results from elastic and qdrant by embeding`. `GET /api/products/search/hybrid?query=term`

- **Delete product** `ResponseEntity<?> deleteProduct(@PathVariable String id) ;` `DELETE /api/products/{id}`
    - Description:  `Deletes a product by its ID.`.


### CustomerController Methods - Endpoints("/api/customers")

- **Add new customer**: `ResponseEntity<CustomerInfo> createCustomerInfo(@RequestBody CustomerInfo customer);` `POST /api/customers`
    - Description: `Adds a new customer to the system by saving their personal information and initializing their order list.`

- **Get all customers** `ResponseEntity<List<CustomerInfo>> getAllCustomers();` `GET /api/customers/all`
    - Description:  `Retrieves all customers stored in the Elasticsearch index.`.

- **Get customer by Id** `ResponseEntity<CustomerInfo> getCustomerById(@PathVariable String id);` `GET /api/customers/{id}`
    - Description:  `Fetches a specific customer’s details by their unique identifier.`.

- **Add order to customer.list** `ResponseEntity<CustomerInfo> addOrderCustomer(@PathVariable String id, @RequestBody String orderId);` `PUT /api/customers/{id}`
    - Description:  `Appends an order ID to an existing customer’s order list using their ID.`.

- **Delete customer by Id** `ResponseEntity<?> deleteCustomerById(@PathVariable String id);` `DELETE /api/customers/{id}`
    - Description:  `Deletes a customer by their ID if they exist in the system.`.


### OrderController Methods - Endpoints("/api/orders")

- **Add new order**: `ResponseEntity<Order> addOrderByCustomerAndProduct(@RequestParam String customerId, @RequestParam List<String> productEans);` `POST /api/orders?customerId={id}&productEans={list}`
    - Description: `Creates a new order for the given customer using a list of product EANs. Links the order with both customer and products.`

- **Get all orders** `ResponseEntity<List<Order>> getAllOrders();` `GET /api/orders/all`
    - Description:  `Retrieves all orders stored in the Elasticsearch orders index.`.

- **Get order by Id** `ResponseEntity<Order> getOrderById(@PathVariable String id);` `GET /api/orders/{id}`
    - Description:  `Fetches a specific order by its ID from Elasticsearch.`.

- **Get order by Id with product info** `ResponseEntity<OrderDto> getOrderByIdWithProducts(@PathVariable String id);` `GET /api/orders/productsInfo/{id}`
    - Description:  `Retrieves detailed information about an order, including product details using a DTO.`.

- **Delete order by Id** `ResponseEntity<String> deleteOrderById(@PathVariable String id)` `DELETE /api/orders/{id}`
    - Description:  `Deletes a specific order by its ID.`.


### QdrantSearchController Methods - Endpoints("/api/qdrant")

- **Find similar products**: ` Mono<Map<String, Object>> findSimilarProducts(@RequestParam String query)` `GET /api/qdrant/similar?query={searchTerm}`
    - Description: ` Generates an embedding for the input query using the embedding service and searches for the top 5 most similar products in the Qdrant vector database.`


### AuthorizationController Methods - Endpoints("/api/auth")

- **Register**: `ResponseEntity<String> register(@RequestBody RegisterRequest request)` `POST /api/auth/register`
    - Description: `Registers a new customer account. It checks if the email is already in use, encrypts the password, assigns a default role, and saves the customer data.`

- **Login** `ResponseEntity<String> login(@RequestBody LoginRequest request);` `POST /api/auth/login`
    - Description:  ` Authenticates the customer using email and password. If valid, generates and returns a JWT token. Otherwise, returns an unauthorized error.`.



## Configuration

### Elasticsearch Client Configuration

- The **GetESClient** class configures the Elasticsearch client using the **ElasticsearchClient** from the Elasticsearch
  Java client library.
- It sets up the connection to an Elasticsearch instance running on `localhost:9200` with **HTTPS**.

---

### HTTP Client Configuration

- The **HttpClientConfigImpl** class configures the HTTP client with SSL and authentication credentials for connecting
  to Elasticsearch.

## Configuration

- **IDE:** IntelliJ IDEA or any Java IDE.
- **Java:** Java 17 or higher.
- **Maven:** Required for building and running the project.
- **Postman:** For testing API endpoints.
- **Elasticsearch:** Version 8.7.0 running on `https://localhost:9200`. HTTPS must be enabled.
- **Qdrant:** Vector similarity search engine running on `http://localhost:6333`.
- **Docker:** Optional, but recommended for running Elasticsearch and Qdrant containers.

## Running the Project

- **Build the Project:** `mvn clean install`
- **Run the Application:** `mvn spring-boot:run`
- **Access the Application:** The application will start on the default Spring Boot port `(8443)..`

## Docker
<details>
  <summary>Create Docker container  "elasticsearch"...(Elasticsearch UI: http://localhost:9200)</summary>

  ```sql
      docker run -d --name elasticsearch \
  -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:7.17.0
  ```
</details>

<details>
  <summary>Create Docker container  "qdrant"...(Qdrant API: http://localhost:6333)</summary>

  ```sql
    docker run -d --name qdrant \
  -p 6333:6333 \
  qdrant/qdrant
  ```
</details>


## Postman

[Link  Postman export](ElasticsearchClient API.postman_collection.json)

## License

- This project is licensed under the MIT License. See the LICENSE file for details.

![img_2.png](logos%2Fimg_2.png)
![img_4.png](logos%2Fimg_4.png)
![img_5.png](logos%2Fimg_5.png)
![img_6.png](logos%2Fimg_6.png)
![img_8.png](logos%2Fimg_8.png)
![img_10.png](logos%2Fimg_10.png)
![img_12.png](logos%2Fimg_12.png)
![img_14.png](logos%2Fimg_14.png)

