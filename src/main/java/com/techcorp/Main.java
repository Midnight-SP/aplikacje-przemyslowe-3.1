package com.techcorp;

import com.techcorp.exception.ApiException;
import com.techcorp.model.Employee;
import com.techcorp.model.ImportSummary;
import com.techcorp.service.ApiService;
import com.techcorp.service.EmployeeService;
import com.techcorp.service.ImportService;

import java.util.List;
import java.util.Map;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("=== TechCorp Employee Management System ===\n");
        
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);
        ApiService apiService = new ApiService();
        
        System.out.println("1. Importowanie pracowników z pliku CSV...");
        ImportSummary summary = importService.importFromCsv("test-data.csv");
        
        System.out.println("\n=== Wyniki importu CSV ===");
        System.out.println("Zaimportowano pracowników: " + summary.getImportedCount());
        System.out.println("Liczba błędów: " + summary.getErrors().size());
        
        if (!summary.getErrors().isEmpty()) {
            System.out.println("\nBłędy podczas importu:");
            for (String error : summary.getErrors()) {
                System.out.println("  - " + error);
            }
        }
        
        System.out.println("\n2. Pobieranie pracowników z REST API...");
        try {
            List<Employee> apiEmployees = apiService.fetchEmployeesFromApi(
                "https://jsonplaceholder.typicode.com/users"
            );
            
            System.out.println("Pobrano " + apiEmployees.size() + " pracowników z API");
            
            int addedCount = 0;
            for (Employee employee : apiEmployees) {
                try {
                    employeeService.addEmployee(employee);
                    addedCount++;
                } catch (Exception e) {
                    System.out.println("  - Pominięto: " + employee.getEmail() + " (" + e.getMessage() + ")");
                }
            }
            System.out.println("Dodano " + addedCount + " pracowników z API do systemu");
            
            System.out.println("\nPrzykładowi pracownicy z API:");
            apiEmployees.stream().limit(3).forEach(emp -> 
                System.out.println("  - " + emp.getFullName() + " (" + emp.getEmail() + ") - " 
                    + emp.getCompanyName() + " - " + emp.getPosition() + " - " + emp.getSalary() + " PLN")
            );
            
        } catch (ApiException e) {
            System.err.println("Błąd podczas pobierania danych z API: " + e.getMessage());
        }
        
        System.out.println("\n=== 3. Walidacja spójności wynagrodzeń ===");
        List<Employee> underpaidEmployees = employeeService.validateSalaryConsistency();
        
        if (underpaidEmployees.isEmpty()) {
            System.out.println("Wszystkie wynagrodzenia są zgodne z bazowymi stawkami stanowisk.");
        } else {
            System.out.println("Pracownicy z wynagrodzeniem niższym niż bazowa stawka stanowiska:");
            for (Employee emp : underpaidEmployees) {
                System.out.printf("  - %s (%s): %.2f PLN (bazowa stawka: %.2f PLN)%n",
                    emp.getFullName(),
                    emp.getPosition(),
                    emp.getSalary(),
                    emp.getPosition().getBaseSalary()
                );
            }
        }
        
        System.out.println("\n=== 4. Statystyki dla firm ===");
        Map<String, com.techcorp.model.CompanyStatistics> companyStats = 
            employeeService.getCompanyStatistics();
        
        companyStats.forEach((companyName, stats) -> {
            System.out.println("\n" + companyName + ":");
            System.out.println("  - Liczba pracowników: " + stats.getEmployeeCount());
            System.out.printf("  - Średnie wynagrodzenie: %.2f PLN%n", stats.getAverageSalary());
            System.out.println("  - Najlepiej opłacany: " + stats.getHighestPaidEmployee());
        });
        
        System.out.println("\n=== 5. Podsumowanie ===");
        System.out.println("Łączna liczba pracowników w systemie: " + employeeService.size());
        
        System.out.println("\nPracownicy posortowani według nazwiska:");
        employeeService.getEmployeesSortedByLastName().forEach(emp -> 
            System.out.println("  - " + emp.getFullName() + " (" + emp.getCompanyName() + ")")
        );
    }
}
