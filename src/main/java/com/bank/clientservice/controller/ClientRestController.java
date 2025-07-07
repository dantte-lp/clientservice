package com.bank.clientservice.controller;

import com.bank.clientservice.dto.ClientDto;
import com.bank.clientservice.dto.HealthCheckDto;
import com.bank.clientservice.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClientRestController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientDto> createClient(@Valid @RequestBody ClientDto clientDto) {
        ClientDto createdClient = clientService.createClient(clientDto);
        return new ResponseEntity<>(createdClient, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients(
            @RequestParam(required = false) String search) {
        List<ClientDto> clients = search != null && !search.isEmpty()
                ? clientService.searchClients(search)
                : clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClientById(@PathVariable UUID id) {
        ClientDto client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @GetMapping("/by-number/{clientNumber}")
    public ResponseEntity<ClientDto> getClientByNumber(@PathVariable String clientNumber) {
        ClientDto client = clientService.getClientByNumber(clientNumber);
        return ResponseEntity.ok(client);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody ClientDto clientDto) {
        ClientDto updatedClient = clientService.updateClient(id, clientDto);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/countries")
    public ResponseEntity<List<String>> getCountries() {
        return ResponseEntity.ok(clientService.getAllCountries());
    }

    @GetMapping("/health")
    public ResponseEntity<HealthCheckDto> healthCheck() {
        HealthCheckDto health = clientService.getHealthCheck();
        HttpStatus status = "UP".equals(health.getStatus())
                ? HttpStatus.OK
                : HttpStatus.SERVICE_UNAVAILABLE;
        return new ResponseEntity<>(health, status);
    }
}