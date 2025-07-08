package com.bank.clientservice.repository;

import com.bank.clientservice.entity.HealthCheckHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HealthCheckHistoryRepository extends JpaRepository<HealthCheckHistory, UUID> {

    List<HealthCheckHistory> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime start, LocalDateTime end);

    HealthCheckHistory findTopByOrderByTimestampDesc();

    @Query("SELECT COUNT(h) FROM HealthCheckHistory h " +
            "WHERE h.status != 'UP' AND h.timestamp BETWEEN :start AND :end")
    long countFailuresBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Modifying
    @Query("DELETE FROM HealthCheckHistory h WHERE h.timestamp < :cutoff")
    int deleteByTimestampBefore(@Param("cutoff") LocalDateTime cutoff);
}