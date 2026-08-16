package com.deliveryflow.service;

import com.deliveryflow.dto.DashboardStatsResponse;
import com.deliveryflow.dto.OrderRequest;
import com.deliveryflow.dto.OrderResponse;
import com.deliveryflow.entity.Customer;
import com.deliveryflow.entity.DeliveryOrder;
import com.deliveryflow.entity.Driver;
import com.deliveryflow.entity.DriverStatus;
import com.deliveryflow.entity.OrderStatus;
import com.deliveryflow.exception.BusinessRuleException;
import com.deliveryflow.exception.ResourceNotFoundException;
import com.deliveryflow.repository.CustomerRepository;
import com.deliveryflow.repository.DriverRepository;
import com.deliveryflow.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Order lifecycle and the rules that govern it.
 *
 * <p>Three rules are enforced here rather than in the controller, so they hold no matter
 * which entry point calls them:
 *
 * <ol>
 *   <li>A driver may only be assigned while they are AVAILABLE.</li>
 *   <li>Assignment moves the order to ASSIGNED and the driver to BUSY, atomically.</li>
 *   <li>Reaching a terminal state (DELIVERED or CANCELLED) releases the driver back to
 *       AVAILABLE — cancelling has to release them too, otherwise drivers would leak into
 *       a permanently BUSY state.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    /** Order numbers are rendered as ORD-1001, ORD-1002, ... rather than raw ids. */
    private static final long ORDER_NUMBER_BASE = 1000L;

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));

        DeliveryOrder order = DeliveryOrder.builder()
                .orderNumber(nextOrderNumber())
                .customer(customer)
                .pickupAddress(request.pickupAddress())
                .deliveryAddress(request.deliveryAddress())
                .status(OrderStatus.CREATED)
                .build();

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAllWithDetails().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return OrderResponse.from(getOrderOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByDriver(Long driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException("Driver", driverId);
        }
        return orderRepository.findByDriverIdWithDetails(driverId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    /**
     * Rule 1 and Rule 2: assign an available driver and move both sides of the relationship
     * in one transaction.
     */
    @Transactional
    public OrderResponse assignDriver(Long orderId, Long driverId) {
        DeliveryOrder order = getOrderOrThrow(orderId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", driverId));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessRuleException(
                    "Order %s is %s and can only be assigned while it is CREATED"
                            .formatted(order.getOrderNumber(), order.getStatus()));
        }
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new BusinessRuleException(
                    "Cannot assign driver %s: driver is currently %s"
                            .formatted(driver.getName(), driver.getStatus()));
        }

        order.setDriver(driver);
        order.setStatus(OrderStatus.ASSIGNED);
        driver.setStatus(DriverStatus.BUSY);

        driverRepository.save(driver);
        return OrderResponse.from(orderRepository.save(order));
    }

    /**
     * Rule 4 and Rule 3: validate the transition against the state machine, then release
     * the driver if the order has reached a terminal state.
     */
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        DeliveryOrder order = getOrderOrThrow(orderId);
        OrderStatus current = order.getStatus();

        if (current == newStatus) {
            throw new BusinessRuleException(
                    "Order %s is already %s".formatted(order.getOrderNumber(), newStatus));
        }
        if (!current.canTransitionTo(newStatus)) {
            throw new BusinessRuleException(
                    "Invalid status transition: %s -> %s. Allowed from %s: %s"
                            .formatted(current, newStatus, current, describeAllowed(current)));
        }
        // ASSIGNED is reached only through the assign endpoint, which also books the driver.
        if (newStatus == OrderStatus.ASSIGNED) {
            throw new BusinessRuleException(
                    "Use PUT /api/orders/{id}/assign/{driverId} to assign a driver");
        }

        order.setStatus(newStatus);

        if (newStatus.isTerminal()) {
            releaseDriver(order);
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    /**
     * Cancelling an order rather than deleting the row: an order that a driver is part-way
     * through delivering is history worth keeping, and the driver still has to be released.
     */
    @Transactional
    public OrderResponse cancel(Long orderId) {
        DeliveryOrder order = getOrderOrThrow(orderId);

        if (order.getStatus().isTerminal()) {
            throw new BusinessRuleException(
                    "Order %s is already %s and cannot be cancelled"
                            .formatted(order.getOrderNumber(), order.getStatus()));
        }

        order.setStatus(OrderStatus.CANCELLED);
        releaseDriver(order);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse dashboardStats() {
        long created = orderRepository.countByStatus(OrderStatus.CREATED);
        long assigned = orderRepository.countByStatus(OrderStatus.ASSIGNED);
        long pickedUp = orderRepository.countByStatus(OrderStatus.PICKED_UP);

        return new DashboardStatsResponse(
                orderRepository.count(),
                created + assigned + pickedUp,
                orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY),
                orderRepository.countByStatus(OrderStatus.DELIVERED),
                orderRepository.countByStatus(OrderStatus.CANCELLED),
                driverRepository.count(),
                driverRepository.findByStatus(DriverStatus.AVAILABLE).size(),
                customerRepository.count()
        );
    }

    private void releaseDriver(DeliveryOrder order) {
        Driver driver = order.getDriver();
        if (driver != null && driver.getStatus() == DriverStatus.BUSY) {
            driver.setStatus(DriverStatus.AVAILABLE);
            driverRepository.save(driver);
        }
    }

    private DeliveryOrder getOrderOrThrow(Long id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private String nextOrderNumber() {
        return "ORD-" + (ORDER_NUMBER_BASE + orderRepository.findMaxOrderId() + 1);
    }

    private String describeAllowed(OrderStatus status) {
        return status.allowedTransitions().isEmpty()
                ? "none (terminal state)"
                : status.allowedTransitions().stream().map(Enum::name).collect(Collectors.joining(", "));
    }
}
