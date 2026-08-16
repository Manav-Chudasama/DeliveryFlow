package com.deliveryflow.dto;

import com.deliveryflow.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(

        @NotNull(message = "Status is required")
        OrderStatus status
) {
}
