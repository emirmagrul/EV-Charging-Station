package com.ev.repository;

import com.ev.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.repository.query.Param;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.driver.id = :driverId ORDER BY n.createdAt DESC")
    List<Notification> findByDriverIdOrderByCreatedAtDesc(@Param("driverId") Long driverId);

    @Query("SELECT n FROM Notification n JOIN n.operator o WHERE o.id = :operatorId ORDER BY n.createdAt DESC")
    List<Notification> findByOperatorIdOrderByCreatedAtDesc(@Param("operatorId") Long operatorId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.driver.id = :driverId AND n.read = false")
    long countByDriverIdAndReadFalse(@Param("driverId") Long driverId);

    @Query("SELECT COUNT(n) FROM Notification n JOIN n.operator o WHERE o.id = :operatorId AND n.read = false")
    long countByOperatorIdAndReadFalse(@Param("operatorId") Long operatorId);
}