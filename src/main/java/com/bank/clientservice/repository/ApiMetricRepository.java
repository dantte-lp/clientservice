package com.bank.clientservice.repository;

import com.bank.clientservice.entity.ApiMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ApiMetricRepository extends JpaRepository<ApiMetric, UUID> {

    List<ApiMetric> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    long countByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(a.responseTimeMs) FROM ApiMetric a " +
            "WHERE a.timestamp BETWEEN :start AND :end")
    Double findAverageResponseTimeBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT a.endpoint, a.method, COUNT(a), AVG(a.responseTimeMs), " +
            "MIN(a.responseTimeMs), MAX(a.responseTimeMs) " +
            "FROM ApiMetric a WHERE a.timestamp BETWEEN :start AND :end " +
            "GROUP BY a.endpoint, a.method ORDER BY COUNT(a) DESC")
    List<Object[]> findEndpointStatistics(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT a.statusCode, COUNT(a) FROM ApiMetric a " +
            "WHERE a.timestamp BETWEEN :start AND :end " +
            "GROUP BY a.statusCode")
    List<Object[]> findStatusCodeDistribution(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Modifying
    @Query("DELETE FROM ApiMetric a WHERE a.timestamp < :cutoff")
    int deleteByTimestampBefore(@Param("cutoff") LocalDateTime cutoff);
}
