package com.bank.clientservice.controller;

import com.bank.clientservice.exception.DatabaseUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.bank.clientservice.controller")
@Slf4j
public class ControllerErrorAdvice {

    @ExceptionHandler(DatabaseUnavailableException.class)
    public String handleDatabaseUnavailable(DatabaseUnavailableException ex, Model model) {
        log.error("Database unavailable in controller: {}", ex.getMessage());

        model.addAttribute("errorTitle", "База данных недоступна");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorDetail", "Пожалуйста, попробуйте позже или обратитесь к администратору.");

        return "error";
    }
}