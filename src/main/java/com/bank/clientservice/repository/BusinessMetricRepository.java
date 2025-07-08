package com.bank.clientservice.repository;

import com.bank.clientservice.entity.BusinessMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessMetricRepository extends JpaRepository<BusinessMetric, UUID> {

    List<BusinessMetric> findByMetricDateBetween(LocalDate start, LocalDate end);

    Optional<BusinessMetric> findByMetricDate(LocalDate date);

    @Query("SELECT SUM(b.newClientsCount) FROM BusinessMetric b " +
            "WHERE b.metricDate BETWEEN :start AND :end")
    Long sumNewClientsBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Modifying
    @Query("DELETE FROM BusinessMetric b WHERE b.metricDate < :cutoff")
    int deleteByMetricDateBefore(@Param("cutoff") LocalDate cutoff);
}