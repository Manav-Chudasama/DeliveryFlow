package com.deliveryflow.entity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Lifecycle state of a delivery order.
 *
 * <p>The legal transitions live here rather than in the service so that there is a single
 * source of truth for the order state machine:
 *
 * <pre>
 * CREATED -> ASSIGNED -> PICKED_UP -> OUT_FOR_DELIVERY -> DELIVERED
 * </pre>
 *
 * <p>Any non-terminal state may also move to {@link #CANCELLED}. {@link #DELIVERED} and
 * {@link #CANCELLED} are terminal.
 */
public enum OrderStatus {
    CREATED,
    ASSIGNED,
    PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED.put(CREATED, EnumSet.of(ASSIGNED, CANCELLED));
        ALLOWED.put(ASSIGNED, EnumSet.of(PICKED_UP, CANCELLED));
        ALLOWED.put(PICKED_UP, EnumSet.of(OUT_FOR_DELIVERY, CANCELLED));
        ALLOWED.put(OUT_FOR_DELIVERY, EnumSet.of(DELIVERED, CANCELLED));
        ALLOWED.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    /** Returns true if an order may move directly from this status to {@code target}. */
    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED.getOrDefault(this, Collections.emptySet()).contains(target);
    }

    /** Returns the statuses reachable from this one, for error messages and the UI. */
    public Set<OrderStatus> allowedTransitions() {
        return Collections.unmodifiableSet(ALLOWED.getOrDefault(this, Collections.emptySet()));
    }

    /** Terminal statuses free up the assigned driver. */
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }
}
