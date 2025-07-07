package com.bank.clientservice.controller;

import com.bank.clientservice.dto.ClientDto;
import com.bank.clientservice.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public String listClients(Model model, @RequestParam(required = false) String search) {
        model.addAttribute("clients",
                search != null && !search.isEmpty()
                        ? clientService.searchClients(search)
                        : clientService.getAllClients());
        model.addAttribute("search", search);
        return "clients/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("client", new ClientDto());
        model.addAttribute("countries", clientService.getAllCountries());
        return "clients/form";
    }

    @PostMapping
    public String createClient(@Valid @ModelAttribute("client") ClientDto clientDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("countries", clientService.getAllCountries());
            return "clients/form";
        }

        try {
            ClientDto createdClient = clientService.createClient(clientDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Клиент успешно создан с номером: " + createdClient.getClientNumber());
            return "redirect:/clients";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при создании клиента: " + e.getMessage());
            model.addAttribute("countries", clientService.getAllCountries());
            return "clients/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model) {
        ClientDto client = clientService.getClientById(id);
        model.addAttribute("client", client);
        model.addAttribute("countries", clientService.getAllCountries());
        return "clients/form";
    }

    @PostMapping("/{id}")
    public String updateClient(@PathVariable UUID id,
                               @Valid @ModelAttribute("client") ClientDto clientDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("countries", clientService.getAllCountries());
            return "clients/form";
        }

        try {
            clientService.updateClient(id, clientDto);
            redirectAttributes.addFlashAttribute("successMessage", "Клиент успешно обновлен");
            return "redirect:/clients";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении клиента: " + e.getMessage());
            model.addAttribute("countries", clientService.getAllCountries());
            return "clients/form";
        }
    }

    @GetMapping("/{id}")
    public String viewClient(@PathVariable UUID id, Model model) {
        ClientDto client = clientService.getClientById(id);
        model.addAttribute("client", client);
        return "clients/view";
    }

    @PostMapping("/{id}/delete")
    public String deleteClient(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            clientService.deleteClient(id);
            redirectAttributes.addFlashAttribute("successMessage", "Клиент успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении клиента: " + e.getMessage());
        }
        return "redirect:/clients";
    }

    @GetMapping("/health")
    public String healthCheck(Model model) {
        model.addAttribute("health", clientService.getHealthCheck());
        return "health";
    }
}