package com.deliveryflow.controller;

import com.deliveryflow.dto.DriverRequest;
import com.deliveryflow.dto.DriverResponse;
import com.deliveryflow.dto.DriverStatusUpdateRequest;
import com.deliveryflow.dto.LocationUpdateRequest;
import com.deliveryflow.dto.OrderResponse;
import com.deliveryflow.service.DriverService;
import com.deliveryflow.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "Manage the delivery fleet")
public class DriverController {

    private final DriverService driverService;
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a driver (starts AVAILABLE)")
    public ResponseEntity<DriverResponse> create(@Valid @RequestBody DriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all drivers")
    public List<DriverResponse> findAll() {
        return driverService.findAll();
    }

    @GetMapping("/available")
    @Operation(summary = "List drivers eligible for assignment")
    public List<DriverResponse> findAvailable() {
        return driverService.findAvailable();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a driver by id")
    public DriverResponse findById(@PathVariable Long id) {
        return driverService.findById(id);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Set a driver AVAILABLE or OFFLINE (BUSY is managed by the order lifecycle)")
    public DriverResponse updateStatus(@PathVariable Long id,
                                       @Valid @RequestBody DriverStatusUpdateRequest request) {
        return driverService.updateStatus(id, request.status());
    }

    @PutMapping("/{id}/location")
    @Operation(summary = "Update a driver's current location")
    public DriverResponse updateLocation(@PathVariable Long id,
                                         @Valid @RequestBody LocationUpdateRequest request) {
        return driverService.updateLocation(id, request);
    }

    @GetMapping("/{id}/orders")
    @Operation(summary = "List the orders assigned to a driver")
    public List<OrderResponse> findOrders(@PathVariable Long id) {
        return orderService.findByDriver(id);
    }
}
