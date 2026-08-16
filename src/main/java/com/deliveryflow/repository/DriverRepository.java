package com.deliveryflow.repository;

import com.deliveryflow.entity.Driver;
import com.deliveryflow.entity.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    boolean existsByPhone(String phone);

    boolean existsByVehicleNumber(String vehicleNumber);

    List<Driver> findByStatus(DriverStatus status);
}
