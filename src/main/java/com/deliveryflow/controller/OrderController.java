package com.deliveryflow.controller;

import com.deliveryflow.dto.DashboardStatsResponse;
import com.deliveryflow.dto.OrderRequest;
import com.deliveryflow.dto.OrderResponse;
import com.deliveryflow.dto.StatusUpdateRequest;
import com.deliveryflow.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Create, assign and track delivery orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create an order (starts CREATED)")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all orders, newest first")
    public List<OrderResponse> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/stats")
    @Operation(summary = "Aggregate counts for the dashboard")
    public DashboardStatsResponse stats() {
        return orderService.dashboardStats();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id")
    public OrderResponse findById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @PutMapping("/{id}/assign/{driverId}")
    @Operation(summary = "Assign an available driver; order becomes ASSIGNED and driver becomes BUSY")
    public OrderResponse assignDriver(@PathVariable Long id, @PathVariable Long driverId) {
        return orderService.assignDriver(id, driverId);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Advance an order through the delivery lifecycle")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody StatusUpdateRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel an order and release its driver")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }
}
