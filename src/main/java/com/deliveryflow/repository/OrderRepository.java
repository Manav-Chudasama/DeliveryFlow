package com.deliveryflow.repository;

import com.deliveryflow.entity.DeliveryOrder;
import com.deliveryflow.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<DeliveryOrder, Long> {

    /**
     * Orders are read with their customer and driver eagerly joined.
     *
     * <p>{@code spring.jpa.open-in-view} is disabled, so the lazy {@code @ManyToOne}
     * associations must be initialised inside the transaction that loads them. The fetch
     * joins do that in a single query and also avoid the N+1 problem when building the
     * list response.
     */
    @Query("""
            select o from DeliveryOrder o
            left join fetch o.customer
            left join fetch o.driver
            order by o.id desc
            """)
    List<DeliveryOrder> findAllWithDetails();

    @Query("""
            select o from DeliveryOrder o
            left join fetch o.customer
            left join fetch o.driver
            where o.id = :id
            """)
    Optional<DeliveryOrder> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            select o from DeliveryOrder o
            left join fetch o.customer
            left join fetch o.driver
            where o.driver.id = :driverId
            order by o.id desc
            """)
    List<DeliveryOrder> findByDriverIdWithDetails(@Param("driverId") Long driverId);

    long countByStatus(OrderStatus status);

    boolean existsByCustomerId(Long customerId);

    /**
     * Highest order id currently stored, used to derive the next human-readable order
     * number. The unique constraint on {@code order_number} is the backstop if two
     * requests ever race here.
     */
    @Query("select coalesce(max(o.id), 0) from DeliveryOrder o")
    long findMaxOrderId();
}
