package com.deliveryflow.service;

import com.deliveryflow.dto.CustomerRequest;
import com.deliveryflow.dto.CustomerResponse;
import com.deliveryflow.entity.Customer;
import com.deliveryflow.exception.BusinessRuleException;
import com.deliveryflow.exception.DuplicateResourceException;
import com.deliveryflow.exception.ResourceNotFoundException;
import com.deliveryflow.repository.CustomerRepository;
import com.deliveryflow.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    "A customer with email %s already exists".formatted(request.email()));
        }

        Customer customer = Customer.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .build();

        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return CustomerResponse.from(getCustomerOrThrow(id));
    }

    /**
     * Customers with existing orders cannot be deleted — the order rows reference them, and
     * silently cascading the delete would destroy delivery history. The caller gets a clear
     * 409 instead of a foreign key error from the database.
     */
    @Transactional
    public void delete(Long id) {
        Customer customer = getCustomerOrThrow(id);
        if (orderRepository.existsByCustomerId(id)) {
            throw new BusinessRuleException(
                    "Cannot delete customer %s because they have existing orders".formatted(customer.getName()));
        }
        customerRepository.delete(customer);
    }

    /** Shared lookup used by other services that need a managed Customer entity. */
    @Transactional(readOnly = true)
    public Customer getCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
