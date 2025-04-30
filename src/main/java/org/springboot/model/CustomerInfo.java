package org.springboot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInfo {

    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private List<String> orderIds;

    public String getRole() {
        if (role == null) {
            return "USER";
        }
        return role;
    }

    @Override
    public String toString() {
        return "CustomerInfo{" +
                "customerId='" + customerId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", orderIds=" + orderIds +
                '}';
    }
}
