package com.deliveryflow.service;

import com.deliveryflow.dto.DriverRequest;
import com.deliveryflow.dto.DriverResponse;
import com.deliveryflow.dto.LocationUpdateRequest;
import com.deliveryflow.entity.Driver;
import com.deliveryflow.entity.DriverStatus;
import com.deliveryflow.exception.BusinessRuleException;
import com.deliveryflow.exception.DuplicateResourceException;
import com.deliveryflow.exception.ResourceNotFoundException;
import com.deliveryflow.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    @Transactional
    public DriverResponse create(DriverRequest request) {
        if (driverRepository.existsByPhone(request.phone())) {
            throw new DuplicateResourceException(
                    "A driver with phone %s already exists".formatted(request.phone()));
        }
        if (driverRepository.existsByVehicleNumber(request.vehicleNumber())) {
            throw new DuplicateResourceException(
                    "A driver with vehicle number %s already exists".formatted(request.vehicleNumber()));
        }

        Driver driver = Driver.builder()
                .name(request.name())
                .phone(request.phone())
                .vehicleNumber(request.vehicleNumber())
                .currentLocation(request.currentLocation())
                .status(DriverStatus.AVAILABLE)
                .build();

        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAll() {
        return driverRepository.findAll().stream()
                .map(DriverResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAvailable() {
        return driverRepository.findByStatus(DriverStatus.AVAILABLE).stream()
                .map(DriverResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse findById(Long id) {
        return DriverResponse.from(getDriverOrThrow(id));
    }

    /**
     * Manual status changes are limited to toggling between AVAILABLE and OFFLINE.
     *
     * <p>BUSY is owned by the order lifecycle: it is set when a driver is assigned and
     * cleared when the order reaches a terminal state. Allowing it to be set or cleared by
     * hand would let a driver be marked available while still mid-delivery, and the
     * assignment guard would then hand them a second order.
     */
    @Transactional
    public DriverResponse updateStatus(Long id, DriverStatus newStatus) {
        Driver driver = getDriverOrThrow(id);

        if (newStatus == DriverStatus.BUSY) {
            throw new BusinessRuleException(
                    "BUSY is set automatically when a driver is assigned to an order and cannot be set manually");
        }
        if (driver.getStatus() == DriverStatus.BUSY) {
            throw new BusinessRuleException(
                    "Driver %s is currently BUSY on a delivery. Complete or cancel that order first."
                            .formatted(driver.getName()));
        }

        driver.setStatus(newStatus);
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional
    public DriverResponse updateLocation(Long id, LocationUpdateRequest request) {
        Driver driver = getDriverOrThrow(id);
        driver.setLatitude(request.latitude());
        driver.setLongitude(request.longitude());
        if (request.currentLocation() != null && !request.currentLocation().isBlank()) {
            driver.setCurrentLocation(request.currentLocation());
        }
        return DriverResponse.from(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public Driver getDriverOrThrow(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", id));
    }
}
