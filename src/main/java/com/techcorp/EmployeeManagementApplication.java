package com.techcorp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
@ImportResource("classpath:employees-beans.xml") // dodasz ten plik w kroku 5
public class EmployeeManagementApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // tu później przeniesiesz logikę z Main: import CSV, xmlEmployees, API, statystyki, walidacja
    }
}