package com.deliveryflow.entity;

/**
 * Lifecycle state of a driver.
 *
 * <p>Only {@link #AVAILABLE} drivers may be assigned to an order. A driver moves to
 * {@link #BUSY} on assignment and back to {@link #AVAILABLE} once the order reaches a
 * terminal state (delivered or cancelled).
 */
public enum DriverStatus {
    AVAILABLE,
    BUSY,
    OFFLINE
}
