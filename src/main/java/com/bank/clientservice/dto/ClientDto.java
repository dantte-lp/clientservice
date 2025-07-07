package com.bank.clientservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {

    private UUID id;

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    private String middleName;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthDate;

    private String clientNumber;

    @NotBlank(message = "Гражданство обязательно")
    private String citizenship;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Вычисляемое поле для отображения полного имени
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        fullName.append(lastName).append(" ").append(firstName);
        if (middleName != null && !middleName.isBlank()) {
            fullName.append(" ").append(middleName);
        }
        return fullName.toString();
    }

    // Вычисляемое поле для возраста
    public int getAge() {
        if (birthDate == null) return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }
}