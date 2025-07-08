package com.bank.clientservice.repository;

import com.bank.clientservice.entity.SystemMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SystemMetricRepository extends JpaRepository<SystemMetric, UUID> {

    List<SystemMetric> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime start, LocalDateTime end);

    List<SystemMetric> findByMetricTypeAndMetricNameAndTimestampBetween(
            String metricType, String metricName, LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM SystemMetric s WHERE s.metricType = :type " +
            "AND s.timestamp BETWEEN :start AND :end ORDER BY s.timestamp")
    List<SystemMetric> findByTypeAndPeriod(
            @Param("type") String type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT AVG(s.metricValue) FROM SystemMetric s " +
            "WHERE s.metricType = :type AND s.metricName = :name " +
            "AND s.timestamp BETWEEN :start AND :end")
    Double findAverageValue(
            @Param("type") String type,
            @Param("name") String name,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Modifying
    @Query("DELETE FROM SystemMetric s WHERE s.timestamp < :cutoff")
    int deleteByTimestampBefore(@Param("cutoff") LocalDateTime cutoff);
}