package com.deliveryflow.dto;

/**
 * Aggregate counts backing the dashboard stat tiles. Computed with {@code countByStatus}
 * queries rather than by loading every order into memory.
 */
public record DashboardStatsResponse(
        long totalOrders,
        long pending,
        long outForDelivery,
        long delivered,
        long cancelled,
        long totalDrivers,
        long availableDrivers,
        long totalCustomers
) {
}
