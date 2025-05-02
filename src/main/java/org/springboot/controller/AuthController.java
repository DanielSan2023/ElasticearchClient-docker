package org.springboot.controller;

import org.springboot.dto.LoginRequest;
import org.springboot.dto.RegisterRequest;
import org.springboot.model.CustomerInfo;
import org.springboot.security.jwt.JwtUtil;
import org.springboot.service.CustomerInfoServiceImpl;
import org.springboot.utility.AppConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("prod")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CustomerInfoServiceImpl customerService;

    public AuthController(JwtUtil jwtUtil, PasswordEncoder passwordEncoder, CustomerInfoServiceImpl customerService) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (customerService.findCustomerByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        CustomerInfo customer = CustomerInfo.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(AppConstants.DEFAULT_CUSTOMER_ROLE)
                .build();

        customerService.saveCustomer(customer);
        return ResponseEntity.ok("Registered");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        return customerService.findCustomerByEmail(request.getEmail())
                .map(customer -> {
                    boolean matches = passwordEncoder.matches(request.getPassword(), customer.getPassword());
                    System.out.println("Heslo z requestu: " + request.getPassword());
                    System.out.println("Uložené heslo: " + customer.getPassword());
                    System.out.println("Match result: " + matches);

                    if (matches) {
                        String token = jwtUtil.generateToken(customer.getCustomerId(), customer.getRole());
                        return ResponseEntity.ok(token);
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
                    }
                })
                .orElseGet(() -> {
                    System.out.println("Email nenájdený: " + request.getEmail());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
                });
    }
}
