package com.bank.clientservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Фамилия обязательна")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "Имя обязательно")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "client_number", unique = true, nullable = false)
    private String clientNumber;

    @NotBlank(message = "Гражданство обязательно")
    @Column(name = "citizenship", nullable = false)
    private String citizenship;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateClientNumber() {
        if (this.clientNumber == null) {
            // Генерация уникального номера клиента в формате: CL-YYYYMMDD-XXXX
            String date = LocalDate.now().toString().replace("-", "");
            String random = String.format("%04d", (int) (Math.random() * 10000));
            this.clientNumber = "CL-" + date + "-" + random;
        }
    }
}