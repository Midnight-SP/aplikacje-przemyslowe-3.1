package com.techcorp.service;

import com.techcorp.exception.DuplicateEmailException;
import com.techcorp.model.Employee;
import com.techcorp.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe dla klasy EmployeeService.
 * Sprawdzają poprawność działania metod zarządzania pracownikami.
 */
class EmployeeServiceTest {
    
    private EmployeeService employeeService;
    
    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
    }
    
    /**
     * Test 1a: Dodawanie pracownika - sprawdzenie rozmiaru
     */
    @Test
    @DisplayName("Powinien zwiększyć rozmiar listy o 1 gdy dodaje pracownika")
    void shouldIncreaseSize_whenAddingEmployee() {
        // Arrange
        Employee employee = new Employee(
            "Jan Kowalski",
            "jan@techcorp.com",
            "TechCorp",
            Position.PROGRAMISTA,
            9000.0
        );
        
        // Act
        employeeService.addEmployee(employee);
        
        // Assert
        assertEquals(1, employeeService.size());
    }
    
    /**
     * Test 1b: Dodawanie pracownika - sprawdzenie emaila
     */
    @Test
    @DisplayName("Powinien zachować email pracownika po dodaniu")
    void shouldPreserveEmail_whenAddingEmployee() {
        // Arrange
        Employee employee = new Employee(
            "Jan Kowalski",
            "jan@techcorp.com",
            "TechCorp",
            Position.PROGRAMISTA,
            9000.0
        );
        
        // Act
        employeeService.addEmployee(employee);
        
        // Assert
        List<Employee> employees = employeeService.getAllEmployees();
        assertEquals("jan@techcorp.com", employees.get(0).getEmail());
    }
    
    /**
     * Test 2a: Rzucenie wyjątku przy duplikacie emaila
     */
    @Test
    @DisplayName("Powinien rzucić wyjątek gdy email już istnieje")
    void shouldThrowException_whenDuplicateEmail() {
        // Arrange
        Employee employee1 = new Employee(
            "Jan Kowalski",
            "jan@techcorp.com",
            "TechCorp",
            Position.PROGRAMISTA,
            9000.0
        );
        Employee employee2 = new Employee(
            "Anna Nowak",
            "JAN@TECHCORP.COM", // Ten sam email, różne wielkości liter
            "DataSoft",
            Position.MANAGER,
            12000.0
        );
        
        employeeService.addEmployee(employee1);
        
        // Act & Assert
        assertThrows(
            DuplicateEmailException.class,
            () -> employeeService.addEmployee(employee2)
        );
    }
    
    /**
     * Test 2b: Weryfikacja komunikatu przy duplikacie emaila
     */
    @Test
    @DisplayName("Powinien zawrzeć email w komunikacie błędu duplikatu")
    void shouldContainEmailInMessage_whenDuplicateEmail() {
        // Arrange
        Employee employee1 = new Employee(
            "Jan Kowalski",
            "jan@techcorp.com",
            "TechCorp",
            Position.PROGRAMISTA,
            9000.0
        );
        Employee employee2 = new Employee(
            "Anna Nowak",
            "JAN@TECHCORP.COM",
            "DataSoft",
            Position.MANAGER,
            12000.0
        );
        
        employeeService.addEmployee(employee1);
        
        // Act
        DuplicateEmailException exception = assertThrows(
            DuplicateEmailException.class,
            () -> employeeService.addEmployee(employee2)
        );
        
        // Assert
        assertTrue(exception.getMessage().toLowerCase().contains("jan@techcorp.com"));
    }
    
    /**
     * Test 2c: Rozmiar listy nie zmienia się przy duplikacie
     */
    @Test
    @DisplayName("Nie powinien zwiększać rozmiaru listy gdy email jest duplikatem")
    void shouldNotIncreaseSize_whenDuplicateEmail() {
        // Arrange
        Employee employee1 = new Employee(
            "Jan Kowalski",
            "jan@techcorp.com",
            "TechCorp",
            Position.PROGRAMISTA,
            9000.0
        );
        Employee employee2 = new Employee(
            "Anna Nowak",
            "JAN@TECHCORP.COM",
            "DataSoft",
            Position.MANAGER,
            12000.0
        );
        
        employeeService.addEmployee(employee1);
        
        // Act
        try {
            employeeService.addEmployee(employee2);
        } catch (DuplicateEmailException e) {
            // Expected
        }
        
        // Assert
        assertEquals(1, employeeService.size());
    }
    
    /**
     * Test 3a: Rzucenie wyjątku przy wartości null
     */
    @Test
    @DisplayName("Powinien rzucić wyjątek gdy pracownik jest null")
    void shouldThrowException_whenNullEmployee() {
        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> employeeService.addEmployee(null)
        );
    }
    
    /**
     * Test 3b: Rozmiar listy nie zmienia się przy null
     */
    @Test
    @DisplayName("Nie powinien zwiększać rozmiaru gdy pracownik jest null")
    void shouldNotIncreaseSize_whenNullEmployee() {
        // Act
        try {
            employeeService.addEmployee(null);
        } catch (NullPointerException e) {
            // Expected
        }
        
        // Assert
        assertEquals(0, employeeService.size());
    }
    
    /**
     * Test 4a: Wyszukiwanie po firmie - ilość pracowników
     */
    @Test
    @DisplayName("Powinien znaleźć właściwą liczbę pracowników dla firmy")
    void shouldFindCorrectCount_whenCompanyExists() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 9000.0
        ));
        employeeService.addEmployee(new Employee(
            "Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000.0
        ));
        employeeService.addEmployee(new Employee(
            "Piotr Wiśniewski", "piotr@test.com", "DataSoft", Position.PREZES, 25000.0
        ));
        
        // Act
        List<Employee> techCorpEmployees = employeeService.findByCompany("TechCorp");
        
        // Assert
        assertEquals(2, techCorpEmployees.size());
    }
    
    /**
     * Test 4b: Wyszukiwanie po firmie - weryfikacja nazwy firmy
     */
    @Test
    @DisplayName("Powinien zwrócić tylko pracowników z właściwej firmy")
    void shouldReturnOnlyMatchingCompany_whenSearching() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 9000.0
        ));
        employeeService.addEmployee(new Employee(
            "Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000.0
        ));
        employeeService.addEmployee(new Employee(
            "Piotr Wiśniewski", "piotr@test.com", "DataSoft", Position.PREZES, 25000.0
        ));
        
        // Act
        List<Employee> techCorpEmployees = employeeService.findByCompany("TechCorp");
        
        // Assert
        assertTrue(techCorpEmployees.stream().allMatch(e -> e.getCompanyName().equals("TechCorp")));
    }
    
    /**
     * Test 5: Wyszukiwanie po firmie - firma nie istnieje
     */
    @Test
    @DisplayName("Powinien zwrócić pustą listę gdy firma nie istnieje")
    void shouldReturnEmptyList_whenCompanyNotExists() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 9000.0
        ));
        
        // Act
        List<Employee> result = employeeService.findByCompany("NieistniejącaFirma");
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    /**
     * Test 6a: Średnie wynagrodzenie - obecność wartości
     */
    @Test
    @DisplayName("Powinien zwrócić niepusty Optional gdy pracownicy istnieją")
    void shouldReturnPresentOptional_whenEmployeesExist() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 8000.0
        ));
        employeeService.addEmployee(new Employee(
            "Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000.0
        ));
        employeeService.addEmployee(new Employee(
            "Piotr Wiśniewski", "piotr@test.com", "DataSoft", Position.PREZES, 20000.0
        ));
        
        // Act
        OptionalDouble average = employeeService.getAverageSalary();
        
        // Assert
        assertTrue(average.isPresent());
    }
    
    /**
     * Test 6b: Średnie wynagrodzenie - wartość
     */
    @Test
    @DisplayName("Powinien obliczyć poprawną średnią wynagrodzeń")
    void shouldCalculateCorrectAverage_whenEmployeesExist() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 8000.0
        ));
        employeeService.addEmployee(new Employee(
            "Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000.0
        ));
        employeeService.addEmployee(new Employee(
            "Piotr Wiśniewski", "piotr@test.com", "DataSoft", Position.PREZES, 20000.0
        ));
        
        // Act
        OptionalDouble average = employeeService.getAverageSalary();
        
        // Assert
        assertEquals(13333.33, average.getAsDouble(), 0.01);
    }
    
    /**
     * Test 7: Średnie wynagrodzenie - brak pracowników
     */
    @Test
    @DisplayName("Powinien zwrócić pusty Optional gdy brak pracowników dla średniej")
    void shouldReturnEmpty_whenNoEmployeesForAverage() {
        // Act
        OptionalDouble average = employeeService.getAverageSalary();
        
        // Assert
        assertFalse(average.isPresent());
    }
    
    /**
     * Test 8a: Najlepiej zarabiający - obecność wartości
     */
    @Test
    @DisplayName("Powinien zwrócić niepusty Optional gdy szuka top earner")
    void shouldReturnPresentOptional_whenFindingTopEarner() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 8000.0
        ));
        employeeService.addEmployee(new Employee(
            "Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000.0
        ));
        employeeService.addEmployee(new Employee(
            "Piotr Wiśniewski", "piotr@test.com", "DataSoft", Position.PREZES, 20000.0
        ));
        
        // Act
        Optional<Employee> topEarner = employeeService.getTopEarner();
        
        // Assert
        assertTrue(topEarner.isPresent());
    }
    
    /**
     * Test 8b: Najlepiej zarabiający - imię
     */
    @Test
    @DisplayName("Powinien znaleźć pracownika z najwyższą pensją - imię")
    void shouldFindTopEarnerName_whenMultipleEmployees() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 8000.0
        ));
        employeeService.addEmployee(new Employee(
            "Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000.0
        ));
        employeeService.addEmployee(new Employee(
            "Piotr Wiśniewski", "piotr@test.com", "DataSoft", Position.PREZES, 20000.0
        ));
        
        // Act
        Optional<Employee> topEarner = employeeService.getTopEarner();
        
        // Assert
        assertEquals("Piotr Wiśniewski", topEarner.get().getFullName());
    }
    
    /**
     * Test 8c: Najlepiej zarabiający - wynagrodzenie
     */
    @Test
    @DisplayName("Powinien znaleźć pracownika z najwyższą pensją - wynagrodzenie")
    void shouldFindTopEarnerSalary_whenMultipleEmployees() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 8000.0
        ));
        employeeService.addEmployee(new Employee(
            "Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000.0
        ));
        employeeService.addEmployee(new Employee(
            "Piotr Wiśniewski", "piotr@test.com", "DataSoft", Position.PREZES, 20000.0
        ));
        
        // Act
        Optional<Employee> topEarner = employeeService.getTopEarner();
        
        // Assert
        assertEquals(20000.0, topEarner.get().getSalary());
    }
    
    /**
     * Test 9: Najlepiej zarabiający - brak pracowników
     */
    @Test
    @DisplayName("Powinien zwrócić pusty Optional gdy brak pracowników dla top earner")
    void shouldReturnEmpty_whenNoEmployeesForTopEarner() {
        // Act
        Optional<Employee> topEarner = employeeService.getTopEarner();
        
        // Assert
        assertFalse(topEarner.isPresent());
    }
    
    /**
     * Test 10a: Walidacja wynagrodzeń - ilość niedopłaconych
     */
    @Test
    @DisplayName("Powinien znaleźć właściwą liczbę niedopłaconych pracowników")
    void shouldFindCorrectCount_whenUnderpaidEmployeesExist() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 7000.0 // Poniżej 8000
        ));
        employeeService.addEmployee(new Employee(
            "Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000.0 // OK (bazowa 10000)
        ));
        employeeService.addEmployee(new Employee(
            "Piotr Wiśniewski", "piotr@test.com", "DataSoft", Position.PREZES, 15000.0 // Poniżej 18000
        ));
        
        // Act
        List<Employee> underpaid = employeeService.validateSalaryConsistency();
        
        // Assert
        assertEquals(2, underpaid.size());
    }
    
    /**
     * Test 10b: Walidacja wynagrodzeń - konkretny niedopłacony (1)
     */
    @Test
    @DisplayName("Powinien znaleźć programistę w liście niedopłaconych")
    void shouldFindProgrammer_whenUnderpaid() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 7000.0
        ));
        employeeService.addEmployee(new Employee(
            "Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000.0
        ));
        employeeService.addEmployee(new Employee(
            "Piotr Wiśniewski", "piotr@test.com", "DataSoft", Position.PREZES, 15000.0
        ));
        
        // Act
        List<Employee> underpaid = employeeService.validateSalaryConsistency();
        
        // Assert
        assertTrue(underpaid.stream().anyMatch(e -> e.getEmail().equals("jan@test.com")));
    }
    
    /**
     * Test 10c: Walidacja wynagrodzeń - konkretny niedopłacony (2)
     */
    @Test
    @DisplayName("Powinien znaleźć prezesa w liście niedopłaconych")
    void shouldFindCEO_whenUnderpaid() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 7000.0
        ));
        employeeService.addEmployee(new Employee(
            "Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000.0
        ));
        employeeService.addEmployee(new Employee(
            "Piotr Wiśniewski", "piotr@test.com", "DataSoft", Position.PREZES, 15000.0
        ));
        
        // Act
        List<Employee> underpaid = employeeService.validateSalaryConsistency();
        
        // Assert
        assertTrue(underpaid.stream().anyMatch(e -> e.getEmail().equals("piotr@test.com")));
    }
    
    /**
     * Test 11: Walidacja wynagrodzeń - wszystkie wynagrodzenia poprawne
     */
    @Test
    @DisplayName("Powinien zwrócić pustą listę gdy wszystkie wynagrodzenia są poprawne")
    void shouldReturnEmptyList_whenAllSalariesValid() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 9000.0
        ));
        employeeService.addEmployee(new Employee(
            "Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000.0
        ));
        
        // Act
        List<Employee> underpaid = employeeService.validateSalaryConsistency();
        
        // Assert
        assertTrue(underpaid.isEmpty());
    }
    
    /**
     * Test 12: Sprawdzenie wielkości liter w emailu (case-insensitive)
     */
    @Test
    @DisplayName("Powinien traktować emaile jako case-insensitive")
    void shouldTreatEmailsAsCaseInsensitive() {
        // Arrange
        Employee employee1 = new Employee(
            "Jan Kowalski", "Jan.Kowalski@TechCorp.COM", "TechCorp", Position.PROGRAMISTA, 9000.0
        );
        Employee employee2 = new Employee(
            "Anna Nowak", "jan.kowalski@techcorp.com", "DataSoft", Position.MANAGER, 12000.0
        );
        
        employeeService.addEmployee(employee1);
        
        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> employeeService.addEmployee(employee2));
    }
    
    /**
     * Test 13a: Case insensitive search - lowercase
     */
    @Test
    @DisplayName("Powinien znaleźć firmę małymi literami")
    void shouldFindByCompany_lowercase() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 9000.0
        ));
        
        // Act
        List<Employee> result = employeeService.findByCompany("techcorp");
        
        // Assert
        assertEquals(1, result.size());
    }
    
    /**
     * Test 13b: Case insensitive search - uppercase
     */
    @Test
    @DisplayName("Powinien znaleźć firmę dużymi literami")
    void shouldFindByCompany_uppercase() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 9000.0
        ));
        
        // Act
        List<Employee> result = employeeService.findByCompany("TECHCORP");
        
        // Assert
        assertEquals(1, result.size());
    }
    
    /**
     * Test 13c: Case insensitive search - mixed case
     */
    @Test
    @DisplayName("Powinien znaleźć firmę mieszanymi literami")
    void shouldFindByCompany_mixedCase() {
        // Arrange
        employeeService.addEmployee(new Employee(
            "Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 9000.0
        ));
        
        // Act
        List<Employee> result = employeeService.findByCompany("TechCorp");
        
        // Assert
        assertEquals(1, result.size());
    }
    
    /**
     * Test 14: Rzucenie wyjątku przy null w findByCompany
     */
    @Test
    @DisplayName("Powinien rzucić wyjątek gdy nazwa firmy jest null w findByCompany")
    void shouldThrowException_whenCompanyNameIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> employeeService.findByCompany(null));
    }
}
