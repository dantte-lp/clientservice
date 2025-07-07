package com.bank.clientservice.repository;

import com.bank.clientservice.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByClientNumber(String clientNumber);

    @Query("SELECT c FROM Client c WHERE " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.middleName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.clientNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.citizenship) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Client> searchClients(@Param("search") String search);

    List<Client> findByCitizenship(String citizenship);

    @Query("SELECT DISTINCT c.citizenship FROM Client c ORDER BY c.citizenship")
    List<String> findAllCitizenships();

    boolean existsByClientNumber(String clientNumber);
}