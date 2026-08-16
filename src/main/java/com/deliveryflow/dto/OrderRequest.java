package com.deliveryflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderRequest(

        @NotNull(message = "Customer id is required")
        Long customerId,

        @NotBlank(message = "Pickup address is required")
        @Size(max = 255, message = "Pickup address must be at most 255 characters")
        String pickupAddress,

        @NotBlank(message = "Delivery address is required")
        @Size(max = 255, message = "Delivery address must be at most 255 characters")
        String deliveryAddress
) {
}
