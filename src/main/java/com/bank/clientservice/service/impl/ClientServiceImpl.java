package com.bank.clientservice.service.impl;

import com.bank.clientservice.dto.ClientDto;
import com.bank.clientservice.dto.HealthCheckDto;
import com.bank.clientservice.entity.Client;
import com.bank.clientservice.exception.ClientNotFoundException;
import com.bank.clientservice.mapper.ClientMapper;
import com.bank.clientservice.repository.ClientRepository;
import com.bank.clientservice.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final DataSource dataSource;

    private static final LocalDateTime START_TIME = LocalDateTime.now();

    @Override
    public ClientDto createClient(ClientDto clientDto) {
        log.info("Creating new client: {} {}", clientDto.getLastName(), clientDto.getFirstName());
        Client client = clientMapper.toEntity(clientDto);
        Client savedClient = clientRepository.save(client);
        log.info("Created client with ID: {} and number: {}", savedClient.getId(), savedClient.getClientNumber());
        return clientMapper.toDto(savedClient);
    }

    @Override
    public ClientDto updateClient(UUID id, ClientDto clientDto) {
        log.info("Updating client with ID: {}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден с ID: " + id));

        clientMapper.updateEntityFromDto(clientDto, client);
        Client updatedClient = clientRepository.save(client);
        log.info("Updated client with ID: {}", id);
        return clientMapper.toDto(updatedClient);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDto getClientById(UUID id) {
        log.info("Fetching client with ID: {}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден с ID: " + id));
        return clientMapper.toDto(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDto getClientByNumber(String clientNumber) {
        log.info("Fetching client with number: {}", clientNumber);
        Client client = clientRepository.findByClientNumber(clientNumber)
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден с номером: " + clientNumber));
        return clientMapper.toDto(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> getAllClients() {
        log.info("Fetching all clients");
        List<Client> clients = clientRepository.findAll();
        return clientMapper.toDtoList(clients);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> searchClients(String search) {
        log.info("Searching clients with query: {}", search);
        if (search == null || search.trim().isEmpty()) {
            return getAllClients();
        }
        List<Client> clients = clientRepository.searchClients(search.trim());
        return clientMapper.toDtoList(clients);
    }

    @Override
    public void deleteClient(UUID id) {
        log.info("Deleting client with ID: {}", id);
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException("Клиент не найден с ID: " + id);
        }
        clientRepository.deleteById(id);
        log.info("Deleted client with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCountries() {
        // Список стран для выбора гражданства
        return Arrays.asList(
                "Российская Федерация",
                "Республика Беларусь",
                "Республика Казахстан",
                "Кыргызская Республика",
                "Республика Узбекистан",
                "Республика Таджикистан",
                "Азербайджанская Республика",
                "Республика Армения",
                "Республика Молдова",
                "Туркменистан",
                "Украина",
                "Грузия",
                "Германия",
                "США",
                "Китай",
                "Турция",
                "Израиль",
                "Великобритания",
                "Франция",
                "Италия",
                "Испания",
                "Польша",
                "Чехия",
                "Другое"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public HealthCheckDto getHealthCheck() {
        HealthCheckDto.HealthCheckDtoBuilder healthCheck = HealthCheckDto.builder()
                .timestamp(LocalDateTime.now())
                .status("UP");

        // Server info
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            long uptime = runtimeMXBean.getUptime();

            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> memory = new HashMap<>();
            memory.put("total", runtime.totalMemory() / 1024 / 1024 + " MB");
            memory.put("free", runtime.freeMemory() / 1024 / 1024 + " MB");
            memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
            memory.put("max", runtime.maxMemory() / 1024 / 1024 + " MB");

            HealthCheckDto.ServerInfo serverInfo = HealthCheckDto.ServerInfo.builder()
                    .hostname(localhost.getHostName())
                    .ipAddress(localhost.getHostAddress())
                    .osName(System.getProperty("os.name"))
                    .osVersion(System.getProperty("os.version"))
                    .javaVersion(System.getProperty("java.version"))
                    .uptime(uptime)
                    .uptimeHours(uptime / (1000 * 60 * 60))
                    .uptimeMinutes((uptime / (1000 * 60)) % 60)
                    .memory(memory)
                    .build();

            healthCheck.server(serverInfo);
        } catch (Exception e) {
            log.error("Error getting server info", e);
        }

        // Database info
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            HealthCheckDto.DatabaseInfo databaseInfo = HealthCheckDto.DatabaseInfo.builder()
                    .status("UP")
                    .url(metaData.getURL())
                    .databaseProductName(metaData.getDatabaseProductName())
                    .databaseProductVersion(metaData.getDatabaseProductVersion())
                    .driverName(metaData.getDriverName())
                    .driverVersion(metaData.getDriverVersion())
                    .clientsCount(clientRepository.count())
                    .build();

            healthCheck.database(databaseInfo);
        } catch (Exception e) {
            log.error("Error getting database info", e);
            HealthCheckDto.DatabaseInfo databaseInfo = HealthCheckDto.DatabaseInfo.builder()
                    .status("DOWN")
                    .build();
            healthCheck.database(databaseInfo);
            healthCheck.status("DEGRADED");
        }

        // Application info
        HealthCheckDto.ApplicationInfo applicationInfo = HealthCheckDto.ApplicationInfo.builder()
                .name("Client Service")
                .version("1.0.0")
                .description("Banking Client Management Service")
                .startTime(START_TIME)
                .build();

        healthCheck.application(applicationInfo);

        return healthCheck.build();
    }
}