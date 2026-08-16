package com.deliveryflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DriverRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 digits")
        String phone,

        @NotBlank(message = "Vehicle number is required")
        @Size(max = 20, message = "Vehicle number must be at most 20 characters")
        String vehicleNumber,

        @Size(max = 255, message = "Location must be at most 255 characters")
        String currentLocation
) {
}
