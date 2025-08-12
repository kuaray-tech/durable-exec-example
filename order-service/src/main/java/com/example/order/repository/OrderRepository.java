package com.example.order.repository;

import com.example.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link com.example.order.entity.Order} entities.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}