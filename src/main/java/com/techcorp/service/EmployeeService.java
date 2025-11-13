package com.techcorp.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.techcorp.exception.DuplicateEmailException;
import com.techcorp.exception.EmployeeNotFoundException;
import com.techcorp.model.CompanyStatistics;
import com.techcorp.model.Employee;
import com.techcorp.model.EmploymentStatus;
import com.techcorp.model.Position;

@Service
public class EmployeeService {
    private final Map<String, Employee> employees = new LinkedHashMap<>();

    public void addEmployee(Employee employee) {
        Objects.requireNonNull(employee, "employee");
        String key = employee.getEmail().toLowerCase();
        if (employees.containsKey(key)) {
            throw new DuplicateEmailException(employee.getEmail());
        }
        employees.put(key, employee);
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees.values());
    }

    public List<Employee> findByCompany(String companyName) {
        Objects.requireNonNull(companyName, "companyName");
        return stream()
                .filter(e -> e.getCompanyName().equalsIgnoreCase(companyName))
                .collect(Collectors.toList());
    }

    public List<Employee> getEmployeesSortedByLastName() {
        Comparator<Employee> cmp = Comparator
                .comparing((Employee e) -> e.getLastName().toLowerCase())
                .thenComparing(e -> e.getFullName().toLowerCase());
        return stream().sorted(cmp).collect(Collectors.toList());
    }

    public Map<Position, List<Employee>> groupByPosition() {
        return stream().collect(Collectors.groupingBy(Employee::getPosition));
    }

    public Map<Position, Long> countByPosition() {
        return stream().collect(Collectors.groupingBy(Employee::getPosition, Collectors.counting()));
    }

    public OptionalDouble getAverageSalary() {
        return employees.values().stream().mapToDouble(Employee::getSalary).average();
    }

    public Optional<Employee> getTopEarner() {
        return stream().max(Comparator.comparingDouble(Employee::getSalary));
    }

    public int size() {
        return employees.size();
    }

    public List<Employee> validateSalaryConsistency() {
        return stream()
                .filter(employee -> employee.getSalary() < employee.getPosition().getBaseSalary())
                .collect(Collectors.toList());
    }

    public Map<String, CompanyStatistics> getCompanyStatistics() {
        return stream()
                .collect(Collectors.groupingBy(
                    Employee::getCompanyName,
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        employeesList -> {
                            long count = employeesList.size();
                            
                            double avgSalary = employeesList.stream()
                                    .mapToDouble(Employee::getSalary)
                                    .average()
                                    .orElse(0.0);
                            
                            String highestPaid = employeesList.stream()
                                    .max(Comparator.comparingDouble(Employee::getSalary))
                                    .map(Employee::getFullName)
                                    .orElse("");
                            
                            return new CompanyStatistics(count, avgSalary, highestPaid);
                        }
                    )
                ));
    }

    public Optional<Employee> findByEmail(String email) {
        Objects.requireNonNull(email, "email");
        return Optional.ofNullable(employees.get(email.toLowerCase()));
    }

    public Employee getByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException(email));
    }

    public void updateEmployee(String email, Employee updatedEmployee) {
        getByEmail(email);
        String key = email.toLowerCase();
        
        if (!email.equalsIgnoreCase(updatedEmployee.getEmail())) {
            String newKey = updatedEmployee.getEmail().toLowerCase();
            if (employees.containsKey(newKey)) {
                throw new DuplicateEmailException(updatedEmployee.getEmail());
            }
            employees.remove(key);
            employees.put(newKey, updatedEmployee);
        } else {
            employees.put(key, updatedEmployee);
        }
    }

    public void deleteEmployee(String email) {
        getByEmail(email);
        employees.remove(email.toLowerCase());
    }

    public void updateEmployeeStatus(String email, EmploymentStatus status) {
        Employee employee = getByEmail(email);
        employee.setStatus(status);
    }

    public List<Employee> findByStatus(EmploymentStatus status) {
        Objects.requireNonNull(status, "status");
        return stream()
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toList());
    }

    public Map<EmploymentStatus, Long> countByStatus() {
        return stream().collect(Collectors.groupingBy(Employee::getStatus, Collectors.counting()));
    }

    private Stream<Employee> stream() {
        return employees.values().stream();
    }
}
