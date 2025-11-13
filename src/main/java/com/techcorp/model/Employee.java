package com.techcorp.model;

import java.util.Objects;

public class Employee {
    private String fullName;
    private String email;
    private String companyName;
    private Position position;
    private double salary;
    private EmploymentStatus status;

    public Employee(String fullName, String email, String companyName, Position position, double salary) {
        this.fullName = Objects.requireNonNull(fullName, "fullName");
        this.email = Objects.requireNonNull(email, "email");
        this.companyName = Objects.requireNonNull(companyName, "companyName");
        this.position = Objects.requireNonNull(position, "position");
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        this.salary = salary;
        this.status = EmploymentStatus.ACTIVE;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = Objects.requireNonNull(fullName, "fullName");
    }

    public String getEmail() {
        return email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = Objects.requireNonNull(companyName, "companyName");
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "position");
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        this.salary = salary;
    }

    public EmploymentStatus getStatus() {
        return status;
    }

    public void setStatus(EmploymentStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public String getLastName() {
        String[] parts = fullName.trim().split("\\s+");
        return parts.length == 0 ? fullName : parts[parts.length - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return email.equalsIgnoreCase(employee.email);
    }

    @Override
    public int hashCode() {
        return email.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return "Employee{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", companyName='" + companyName + '\'' +
                ", position=" + position +
                ", salary=" + salary +
                ", status=" + status +
                '}';
    }
}
