package com.bank.clientservice.service;

import com.bank.clientservice.dto.ClientDto;
import com.bank.clientservice.dto.HealthCheckDto;

import java.util.List;
import java.util.UUID;

public interface ClientService {

    ClientDto createClient(ClientDto clientDto);

    ClientDto updateClient(UUID id, ClientDto clientDto);

    ClientDto getClientById(UUID id);

    ClientDto getClientByNumber(String clientNumber);

    List<ClientDto> getAllClients();

    List<ClientDto> searchClients(String search);

    void deleteClient(UUID id);

    List<String> getAllCountries();

    HealthCheckDto getHealthCheck();
}