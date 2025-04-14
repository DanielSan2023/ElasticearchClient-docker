package org.springboot.service;

import org.springboot.model.CustomerInfo;

public interface CustomerInfoService {
    CustomerInfo saveCustomer(CustomerInfo customer);

    CustomerInfo addOrderToCustomer(String id, String orderId);

    boolean deleteCustomerBuId(String id);
}
