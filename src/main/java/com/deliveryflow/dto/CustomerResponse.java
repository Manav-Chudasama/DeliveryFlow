package com.deliveryflow.dto;

import com.deliveryflow.entity.Customer;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        LocalDateTime createdAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCreatedAt()
        );
    }
}
