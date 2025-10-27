package com.techcorp.service;

import com.techcorp.model.Employee;
import com.techcorp.model.ImportSummary;
import com.techcorp.model.Position;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportService {
    private final EmployeeService employeeService;
    
    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    public ImportSummary importFromCsv(String filepath) {
        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        int lineNumber = 0;
        
    try (BufferedReader reader = Files.newBufferedReader(Path.of(filepath), StandardCharsets.UTF_8)) {
            String line;
            
            reader.readLine();
            lineNumber++;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    Employee employee = parseCsvLine(line, lineNumber);
                    employeeService.addEmployee(employee);
                    importedCount++;
                } catch (Exception e) {
                    errors.add("Linia " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            errors.add("Błąd odczytu pliku: " + e.getMessage());
        }
        
        return new ImportSummary(importedCount, errors);
    }
    
    private Employee parseCsvLine(String line, int lineNumber) {
        String[] fields = line.split(",");
        
        if (fields.length != 6) {
            throw new IllegalArgumentException("Nieprawidłowa liczba kolumn (oczekiwano 6, otrzymano " + fields.length + ")");
        }
        
        String firstName = fields[0].trim();
        String lastName = fields[1].trim();
        String email = fields[2].trim();
        String company = fields[3].trim();
        String positionStr = fields[4].trim();
        String salaryStr = fields[5].trim();
        
        Position position;
        try {
            position = Position.valueOf(positionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nieprawidłowe stanowisko: " + positionStr);
        }
        
        double salary;
        try {
            salary = Double.parseDouble(salaryStr);
            if (salary <= 0) {
                throw new IllegalArgumentException("Wynagrodzenie musi być dodatnie");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Nieprawidłowy format wynagrodzenia: " + salaryStr);
        }
        
        String fullName = firstName + " " + lastName;
        return new Employee(fullName, email, company, position, salary);
    }
}
