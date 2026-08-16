package com.deliveryflow.dto;

import com.deliveryflow.entity.DriverStatus;
import jakarta.validation.constraints.NotNull;

public record DriverStatusUpdateRequest(

        @NotNull(message = "Status is required")
        DriverStatus status
) {
}
