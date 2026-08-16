package com.deliveryflow.dto;

import com.deliveryflow.entity.DeliveryOrder;
import com.deliveryflow.entity.Driver;
import com.deliveryflow.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Flattened view of an order.
 *
 * <p>The customer and driver are reduced to id/name pairs so the response is a flat row the
 * UI can render directly, with no nested entity graphs and no lazy proxies reaching the
 * serialiser.
 *
 * <p>{@code allowedTransitions} is derived from {@link OrderStatus}, which lets the frontend
 * render exactly the actions the backend will accept instead of duplicating the state
 * machine in JavaScript.
 */
public record OrderResponse(
        Long id,
        String orderNumber,
        Long customerId,
        String customerName,
        Long driverId,
        String driverName,
        String pickupAddress,
        String deliveryAddress,
        OrderStatus status,
        List<OrderStatus> allowedTransitions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderResponse from(DeliveryOrder order) {
        Driver driver = order.getDriver();
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                driver != null ? driver.getId() : null,
                driver != null ? driver.getName() : null,
                order.getPickupAddress(),
                order.getDeliveryAddress(),
                order.getStatus(),
                List.copyOf(order.getStatus().allowedTransitions()),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
