package com.techcorp;

import com.techcorp.exception.ApiException;
import com.techcorp.model.CompanyStatistics;
import com.techcorp.model.Employee;
import com.techcorp.model.ImportSummary;
import com.techcorp.service.ApiService;
import com.techcorp.service.EmployeeService;
import com.techcorp.service.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy integracyjne sprawdzające poprawne działanie całego programu.
 * Testują współpracę wszystkich komponentów systemu.
 */
class IntegrationTest {
    
    private EmployeeService employeeService;
    private ImportService importService;
    private ApiService apiService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
        importService = new ImportService(employeeService);
        apiService = new ApiService();
    }
    
    /**
     * Test 1: Import z CSV + walidacja + statystyki
     * Sprawdza kompletny przepływ: import, walidacja wynagrodzeń, generowanie statystyk
     */
    @Test
    void testCompleteCSVImportWithValidationAndStatistics() throws IOException {
        Path csvFile = tempDir.resolve("test-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@techcorp.com,TechCorp,MANAGER,15000
                Piotr,Wiśniewski,piotr@datasoft.com,DataSoft,PROGRAMISTA,7500
                Maria,Kowalczyk,maria@datasoft.com,DataSoft,WICEPREZES,20000
                """;
        Files.writeString(csvFile, csvContent);
        
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        assertEquals(4, summary.getImportedCount(), "Powinno zaimportować 4 pracowników");
        assertTrue(summary.getErrors().isEmpty(), "Nie powinno być błędów");
        assertEquals(4, employeeService.size(), "EmployeeService powinien mieć 4 pracowników");
        
        List<Employee> underpaidEmployees = employeeService.validateSalaryConsistency();
        
        assertEquals(1, underpaidEmployees.size(), "Jeden pracownik ma wynagrodzenie poniżej bazowej stawki");
        assertEquals("piotr@datasoft.com", underpaidEmployees.get(0).getEmail().toLowerCase());
        
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
        
        assertEquals(2, stats.size(), "Powinny być statystyki dla 2 firm");
        
        CompanyStatistics techCorpStats = stats.get("TechCorp");
        assertNotNull(techCorpStats);
        assertEquals(2, techCorpStats.getEmployeeCount());
        assertEquals(12000.0, techCorpStats.getAverageSalary(), 0.01);
        assertEquals("Anna Nowak", techCorpStats.getHighestPaidEmployee());
        
        CompanyStatistics dataSoftStats = stats.get("DataSoft");
        assertNotNull(dataSoftStats);
        assertEquals(2, dataSoftStats.getEmployeeCount());
        assertEquals(13750.0, dataSoftStats.getAverageSalary(), 0.01);
        assertEquals("Maria Kowalczyk", dataSoftStats.getHighestPaidEmployee());
    }
    
    /**
     * Test 2: Import z CSV z błędami
     * Sprawdza czy system poprawnie obsługuje błędne dane
     */
    @Test
    void testCSVImportWithErrors() throws IOException {
        Path csvFile = tempDir.resolve("test-errors.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@test.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@test.com,DataSoft,INVALID_POSITION,10000
                Piotr,Wiśniewski,piotr@test.com,CloudInc,MANAGER,-5000
                Maria,Kowalczyk,maria@test.com,TechCorp,PREZES,abc
                
                Katarzyna,Dąbrowska,katarzyna@test.com,TechCorp,WICEPREZES,20000
                """;
        Files.writeString(csvFile, csvContent);
        
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        assertEquals(2, summary.getImportedCount(), "Powinno zaimportować 2 poprawnych pracowników");
        assertEquals(3, summary.getErrors().size(), "Powinno być 3 błędy");
        
        assertTrue(summary.getErrors().stream().anyMatch(e -> e.contains("INVALID_POSITION")));
        assertTrue(summary.getErrors().stream().anyMatch(e -> e.contains("dodatnie")));
        assertTrue(summary.getErrors().stream().anyMatch(e -> e.contains("format")));
    }
    
    /**
     * Test 3: Integracja z REST API
     * Sprawdza czy system poprawnie pobiera i przetwarza dane z API
     */
    @Test
    void testAPIIntegrationWithEmployeeService() throws ApiException {
        String apiUrl = "https://jsonplaceholder.typicode.com/users";
        
        List<Employee> apiEmployees = apiService.fetchEmployeesFromApi(apiUrl);
        
        assertEquals(10, apiEmployees.size(), "API powinno zwrócić 10 użytkowników");
        
        Employee firstEmployee = apiEmployees.get(0);
        assertNotNull(firstEmployee.getFullName());
        assertNotNull(firstEmployee.getEmail());
        assertNotNull(firstEmployee.getCompanyName());
        assertEquals("PROGRAMISTA", firstEmployee.getPosition().name());
        assertEquals(8000.0, firstEmployee.getSalary(), 0.01);
        
        int addedCount = 0;
        for (Employee emp : apiEmployees) {
            try {
                employeeService.addEmployee(emp);
                addedCount++;
            } catch (Exception e) {
            }
        }
        
        assertEquals(10, addedCount, "Wszyscy 10 pracowników powinno być dodanych");
        assertEquals(10, employeeService.size());
        
        List<Employee> underpaid = employeeService.validateSalaryConsistency();
        
        assertTrue(underpaid.isEmpty(), "Pracownicy z API mają bazowe wynagrodzenie");
    }
    
    /**
     * Test 4: Kompletny scenariusz - CSV + API + analiza
     * Sprawdza pełny przepływ pracy systemu
     */
    @Test
    void testCompleteWorkflow() throws IOException, ApiException {
        Path csvFile = tempDir.resolve("initial-data.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@techcorp.com,TechCorp,MANAGER,15000
                """;
        Files.writeString(csvFile, csvContent);
        
        ImportSummary csvSummary = importService.importFromCsv(csvFile.toString());
        assertEquals(2, csvSummary.getImportedCount());
        
        List<Employee> apiEmployees = apiService.fetchEmployeesFromApi(
            "https://jsonplaceholder.typicode.com/users"
        );
        
        int apiAddedCount = 0;
        for (Employee emp : apiEmployees) {
            try {
                employeeService.addEmployee(emp);
                apiAddedCount++;
            } catch (Exception e) {
            }
        }
        
        int totalExpected = csvSummary.getImportedCount() + apiAddedCount;
        assertEquals(totalExpected, employeeService.size());
        assertTrue(employeeService.size() >= 12, "Powinno być co najmniej 12 pracowników");
        
        List<Employee> underpaid = employeeService.validateSalaryConsistency();
        assertNotNull(underpaid);
        
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        assertTrue(stats.size() >= 2, "Powinno być co najmniej 2 firmy");
        
        assertTrue(employeeService.getAverageSalary().isPresent());
        assertTrue(employeeService.getTopEarner().isPresent());
        
        List<Employee> sorted = employeeService.getEmployeesSortedByLastName();
        assertEquals(employeeService.size(), sorted.size());
        
        Map<com.techcorp.model.Position, Long> byPosition = employeeService.countByPosition();
        assertFalse(byPosition.isEmpty());
    }
    
    /**
     * Test 5: Obsługa duplikatów email
     * Sprawdza czy system poprawnie wykrywa duplikaty
     */
    @Test
    void testDuplicateEmailHandling() throws IOException {
        Path csvFile = tempDir.resolve("duplicates.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@test.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,JAN@TEST.COM,DataSoft,MANAGER,15000
                """;
        Files.writeString(csvFile, csvContent);
        
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        assertEquals(1, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).toLowerCase().contains("istnieje"));
    }
    
    /**
     * Test 6: Puste pliki i edge cases
     */
    @Test
    void testEmptyAndEdgeCases() throws IOException {
        Path emptyFile = tempDir.resolve("empty.csv");
        Files.writeString(emptyFile, "firstName,lastName,email,company,position,salary\n");
        
        ImportSummary emptySummary = importService.importFromCsv(emptyFile.toString());
        assertEquals(0, emptySummary.getImportedCount());
        assertTrue(emptySummary.getErrors().isEmpty());
        
        Path blankFile = tempDir.resolve("blank.csv");
        Files.writeString(blankFile, "firstName,lastName,email,company,position,salary\n\n\n\n");
        
        ImportSummary blankSummary = importService.importFromCsv(blankFile.toString());
        assertEquals(0, blankSummary.getImportedCount());
        assertTrue(blankSummary.getErrors().isEmpty());
        
        Map<String, CompanyStatistics> emptyStats = employeeService.getCompanyStatistics();
        assertTrue(emptyStats.isEmpty());
        
        List<Employee> emptyUnderpaid = employeeService.validateSalaryConsistency();
        assertTrue(emptyUnderpaid.isEmpty());
    }
    
    /**
     * Test 7: Różne formaty nazwisk w CSV
     */
    @Test
    void testVariousNameFormats() throws IOException {
        Path csvFile = tempDir.resolve("names.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@test.com,TechCorp,PROGRAMISTA,9000
                Anna Maria,Nowak-Kowalska,anna@test.com,DataSoft,MANAGER,15000
                Piotr,Wiśniewski,piotr@test.com,CloudInc,PREZES,30000
                """;
        Files.writeString(csvFile, csvContent);
        
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        assertEquals(3, summary.getImportedCount());
        
        List<Employee> employees = employeeService.getAllEmployees();
        assertTrue(employees.stream().anyMatch(e -> e.getFullName().contains("Anna Maria")));
        assertTrue(employees.stream().anyMatch(e -> e.getFullName().contains("Nowak-Kowalska")));
    }
    
    /**
     * Test 8: Sortowanie i filtrowanie
     */
    @Test
    void testSortingAndFiltering() throws IOException {
        Path csvFile = tempDir.resolve("sorting.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Zbigniew,Adamczyk,z@test.com,TechCorp,PROGRAMISTA,9000
                Anna,Zielińska,a@test.com,TechCorp,MANAGER,15000
                Marek,Adamczyk,m@test.com,DataSoft,PREZES,30000
                """;
        Files.writeString(csvFile, csvContent);
        
        importService.importFromCsv(csvFile.toString());
        
        List<Employee> sorted = employeeService.getEmployeesSortedByLastName();
        assertTrue(sorted.get(0).getLastName().equals("Adamczyk"));
        assertTrue(sorted.get(1).getLastName().equals("Adamczyk"));
        assertEquals("Anna Zielińska", sorted.get(2).getFullName());
        
        List<Employee> techCorpEmployees = employeeService.findByCompany("TechCorp");
        assertEquals(2, techCorpEmployees.size());
        
        var grouped = employeeService.groupByPosition();
        assertTrue(grouped.containsKey(com.techcorp.model.Position.PROGRAMISTA));
        assertTrue(grouped.containsKey(com.techcorp.model.Position.MANAGER));
        assertTrue(grouped.containsKey(com.techcorp.model.Position.PREZES));
    }
}
