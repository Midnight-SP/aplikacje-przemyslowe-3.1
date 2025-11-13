package com.techcorp.controller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techcorp.dto.CompanyStatisticsDTO;
import com.techcorp.model.CompanyStatistics;
import com.techcorp.model.Employee;
import com.techcorp.model.EmploymentStatus;
import com.techcorp.model.Position;
import com.techcorp.service.EmployeeService;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final EmployeeService employeeService;

    public StatisticsController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/salary/average")
    public ResponseEntity<Map<String, Double>> getAverageSalary(
            @RequestParam(required = false) String company) {
        
        double average;
        
        if (company != null && !company.isBlank()) {
            List<Employee> employees = employeeService.findByCompany(company);
            average = employees.stream()
                    .mapToDouble(Employee::getSalary)
                    .average()
                    .orElse(0.0);
        } else {
            average = employeeService.getAverageSalary().orElse(0.0);
        }
        
        Map<String, Double> result = new HashMap<>();
        result.put("averageSalary", average);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/company/{companyName}")
    public ResponseEntity<CompanyStatisticsDTO> getCompanyStatistics(
            @PathVariable String companyName) {
        
        Map<String, CompanyStatistics> allStats = employeeService.getCompanyStatistics();
        CompanyStatistics stats = allStats.get(companyName);
        
        if (stats == null) {
            List<Employee> employees = employeeService.findByCompany(companyName);
            if (employees.isEmpty()) {
                throw new IllegalArgumentException("Company not found: " + companyName);
            }
            
            long count = employees.size();
            double avgSalary = employees.stream()
                    .mapToDouble(Employee::getSalary)
                    .average()
                    .orElse(0.0);
            
            Employee topEarner = employees.stream()
                    .max(Comparator.comparingDouble(Employee::getSalary))
                    .orElse(null);
            
            double highestSalary = topEarner != null ? topEarner.getSalary() : 0.0;
            String topEarnerName = topEarner != null ? topEarner.getFullName() : "";
            
            CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                    companyName,
                    count,
                    avgSalary,
                    highestSalary,
                    topEarnerName
            );
            
            return ResponseEntity.ok(dto);
        }
        
        List<Employee> employees = employeeService.findByCompany(companyName);
        double highestSalary = employees.stream()
                .mapToDouble(Employee::getSalary)
                .max()
                .orElse(0.0);
        
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(
                companyName,
                stats.getEmployeeCount(),
                stats.getAverageSalary(),
                highestSalary,
                stats.getHighestPaidEmployee()
        );
        
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/positions")
    public ResponseEntity<Map<Position, Long>> getPositionStatistics() {
        Map<Position, Long> stats = employeeService.countByPosition();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<EmploymentStatus, Long>> getStatusStatistics() {
        Map<EmploymentStatus, Long> stats = employeeService.countByStatus();
        return ResponseEntity.ok(stats);
    }
}
