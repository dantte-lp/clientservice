package com.bank.clientservice.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.bank.clientservice.repository")
@EntityScan(basePackages = "com.bank.clientservice.entity")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
}