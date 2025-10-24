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
    
    @Test
    void shouldImportCorrectCountFromCSV() throws IOException {
        Path csvFile = tempDir.resolve("test-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@techcorp.com,TechCorp,MANAGER,15000
                """;
        Files.writeString(csvFile, csvContent);
        
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        assertEquals(2, summary.getImportedCount());
    }
    
    @Test
    void shouldHaveNoErrorsForValidCSV() throws IOException {
        Path csvFile = tempDir.resolve("test-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                """;
        Files.writeString(csvFile, csvContent);
        
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        assertTrue(summary.getErrors().isEmpty());
    }
    
    @Test
    void shouldAddEmployeesToService() throws IOException {
        Path csvFile = tempDir.resolve("test-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@techcorp.com,TechCorp,MANAGER,15000
                """;
        Files.writeString(csvFile, csvContent);
        
        importService.importFromCsv(csvFile.toString());
        
        assertEquals(2, employeeService.size());
    }
    
    @Test
    void shouldFindUnderpaidEmployees() throws IOException {
        Path csvFile = tempDir.resolve("test-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Piotr,Wiśniewski,piotr@datasoft.com,DataSoft,PROGRAMISTA,7500
                """;
        Files.writeString(csvFile, csvContent);
        
        importService.importFromCsv(csvFile.toString());
        List<Employee> underpaidEmployees = employeeService.validateSalaryConsistency();
        
        assertEquals(1, underpaidEmployees.size());
    }
    
    @Test
    void shouldGenerateCompanyStatistics() throws IOException {
        Path csvFile = tempDir.resolve("test-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,15000
                """;
        Files.writeString(csvFile, csvContent);
        
        importService.importFromCsv(csvFile.toString());
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
        
        assertEquals(2, stats.size());
    }
    
    @Test
    void shouldImportOnlyValidRecordsWhenErrors() throws IOException {
        Path csvFile = tempDir.resolve("test-errors.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@test.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@test.com,DataSoft,INVALID_POSITION,10000
                """;
        Files.writeString(csvFile, csvContent);
        
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        assertEquals(1, summary.getImportedCount());
    }
    
    @Test
    void shouldReportAllErrors() throws IOException {
        Path csvFile = tempDir.resolve("test-errors.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Anna,Nowak,anna@test.com,DataSoft,INVALID_POSITION,10000
                Piotr,Wiśniewski,piotr@test.com,CloudInc,MANAGER,-5000
                """;
        Files.writeString(csvFile, csvContent);
        
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        assertEquals(2, summary.getErrors().size());
    }
    
    @Test
    void shouldFetch10EmployeesFromAPI() throws ApiException {
        String apiUrl = "https://jsonplaceholder.typicode.com/users";
        
        List<Employee> apiEmployees = apiService.fetchEmployeesFromApi(apiUrl);
        
        assertEquals(10, apiEmployees.size());
    }
    
    @Test
    void shouldHandleDuplicateEmail() throws IOException {
        Path csvFile = tempDir.resolve("duplicates.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@test.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,JAN@TEST.COM,DataSoft,MANAGER,15000
                """;
        Files.writeString(csvFile, csvContent);
        
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        assertEquals(1, summary.getImportedCount());
    }
    
    @Test
    void shouldHandleEmptyCSV() throws IOException {
        Path emptyFile = tempDir.resolve("empty.csv");
        Files.writeString(emptyFile, "firstName,lastName,email,company,position,salary\n");
        
        ImportSummary emptySummary = importService.importFromCsv(emptyFile.toString());
        
        assertEquals(0, emptySummary.getImportedCount());
    }
}
