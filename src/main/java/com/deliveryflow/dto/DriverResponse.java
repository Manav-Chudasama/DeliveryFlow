package com.deliveryflow.dto;

import com.deliveryflow.entity.Driver;
import com.deliveryflow.entity.DriverStatus;

import java.time.LocalDateTime;

public record DriverResponse(
        Long id,
        String name,
        String phone,
        String vehicleNumber,
        DriverStatus status,
        String currentLocation,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt
) {
    public static DriverResponse from(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getName(),
                driver.getPhone(),
                driver.getVehicleNumber(),
                driver.getStatus(),
                driver.getCurrentLocation(),
                driver.getLatitude(),
                driver.getLongitude(),
                driver.getCreatedAt()
        );
    }
}
