package com.techcorp.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techcorp.dto.EmployeeDTO;
import com.techcorp.model.Employee;
import com.techcorp.model.EmploymentStatus;
import com.techcorp.service.EmployeeService;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(
            @RequestParam(required = false) String company) {
        List<Employee> employees;
        
        if (company != null && !company.isBlank()) {
            employees = employeeService.findByCompany(company);
        } else {
            employees = employeeService.getAllEmployees();
        }
        
        List<EmployeeDTO> dtos = employees.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{email}")
    public ResponseEntity<EmployeeDTO> getEmployeeByEmail(@PathVariable String email) {
        Employee employee = employeeService.getByEmail(email);
        return ResponseEntity.ok(toDTO(employee));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByStatus(@PathVariable EmploymentStatus status) {
        List<Employee> employees = employeeService.findByStatus(status);
        List<EmployeeDTO> dtos = employees.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO dto) {
        Employee employee = fromDTO(dto);
        employeeService.addEmployee(employee);
        
        EmployeeDTO responseDTO = toDTO(employee);
        URI location = URI.create("/api/employees/" + employee.getEmail());
        
        return ResponseEntity.created(location).body(responseDTO);
    }

    @PutMapping("/{email}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable String email, 
            @RequestBody EmployeeDTO dto) {
        Employee employee = fromDTO(dto);
        employeeService.updateEmployee(email, employee);
        
        return ResponseEntity.ok(toDTO(employee));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String email) {
        employeeService.deleteEmployee(email);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{email}/status")
    public ResponseEntity<EmployeeDTO> updateEmployeeStatus(
            @PathVariable String email,
            @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        EmploymentStatus status = EmploymentStatus.valueOf(statusStr);
        
        employeeService.updateEmployeeStatus(email, status);
        Employee employee = employeeService.getByEmail(email);
        
        return ResponseEntity.ok(toDTO(employee));
    }

    private EmployeeDTO toDTO(Employee employee) {
        String[] names = employee.getFullName().trim().split("\\s+", 2);
        String firstName = names[0];
        String lastName = names.length > 1 ? names[1] : "";
        
        return new EmployeeDTO(
                firstName,
                lastName,
                employee.getEmail(),
                employee.getCompanyName(),
                employee.getPosition(),
                employee.getSalary(),
                employee.getStatus()
        );
    }

    private Employee fromDTO(EmployeeDTO dto) {
        String fullName = dto.getFirstName() + " " + dto.getLastName();
        Employee employee = new Employee(
                fullName.trim(),
                dto.getEmail(),
                dto.getCompany(),
                dto.getPosition(),
                dto.getSalary()
        );
        
        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
        }
        
        return employee;
    }
}
