package com.techcorp.service;

import com.techcorp.model.Employee;
import com.techcorp.model.ImportSummary;
import com.techcorp.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe dla klasy ImportService.
 * Testują import danych z plików CSV z obsługą błędów.
 */
class ImportServiceTest {
    
    private EmployeeService employeeService;
    private ImportService importService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
        importService = new ImportService(employeeService);
    }
    
    /**
     * Test 1a: Poprawny import - liczba zaimportowanych
     */
    @Test
    @DisplayName("Powinien zaimportować wszystkie poprawne rekordy - liczba")
    void shouldImportCorrectCount_whenValidCSV() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("valid-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,WICEPREZES,16000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(3, summary.getImportedCount(), "Powinno zaimportować 3 pracowników");
    }
    
    /**
     * Test 1b: Poprawny import - brak błędów
     */
    @Test
    @DisplayName("Nie powinno być błędów gdy CSV poprawny")
    void shouldHaveNoErrors_whenValidCSV() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("valid-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,WICEPREZES,16000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertTrue(summary.getErrors().isEmpty(), "Nie powinno być błędów");
    }
    
    /**
     * Test 1c: Poprawny import - rozmiar w EmployeeService
     */
    @Test
    @DisplayName("EmployeeService powinien mieć właściwą liczbę pracowników po imporcie")
    void shouldHaveCorrectServiceSize_whenValidCSV() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("valid-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,WICEPREZES,16000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(3, employeeService.size(), "EmployeeService powinien mieć 3 pracowników");
    }
    
    /**
     * Test 1d: Poprawny import - konkretny pracownik (1)
     */
    @Test
    @DisplayName("Powinien zaimportować Jana Kowalskiego")
    void shouldImportJanKowalski_whenValidCSV() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("valid-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,WICEPREZES,16000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        importService.importFromCsv(csvFile.toString());
        
        // Assert
        List<Employee> employees = employeeService.getAllEmployees();
        assertTrue(employees.stream().anyMatch(e -> e.getEmail().equals("jan@techcorp.com")));
    }
    
    /**
     * Test 1e: Poprawny import - konkretny pracownik (2)
     */
    @Test
    @DisplayName("Powinien zaimportować Annę Nowak")
    void shouldImportAnnaNowak_whenValidCSV() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("valid-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,WICEPREZES,16000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        importService.importFromCsv(csvFile.toString());
        
        // Assert
        List<Employee> employees = employeeService.getAllEmployees();
        assertTrue(employees.stream().anyMatch(e -> e.getEmail().equals("anna@datasoft.com")));
    }
    
    /**
     * Test 1f: Poprawny import - konkretny pracownik (3)
     */
    @Test
    @DisplayName("Powinien zaimportować Piotra Wiśniewskiego")
    void shouldImportPiotrWisniewski_whenValidCSV() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("valid-employees.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,WICEPREZES,16000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        importService.importFromCsv(csvFile.toString());
        
        // Assert
        List<Employee> employees = employeeService.getAllEmployees();
        assertTrue(employees.stream().anyMatch(e -> e.getEmail().equals("piotr@cloudinc.com")));
    }
    
    /**
     * Test 2a: Niepoprawne stanowisko - liczba zaimportowanych
     */
    @Test
    @DisplayName("Powinien zaimportować tylko poprawne rekordy gdy stanowisko niepoprawne")
    void shouldImportOnlyValid_whenInvalidPosition() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("invalid-position.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POSITION,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,15000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(2, summary.getImportedCount(), "Powinno zaimportować 2 poprawnych pracowników");
    }
    
    /**
     * Test 2b: Niepoprawne stanowisko - liczba błędów
     */
    @Test
    @DisplayName("Powinien zgłosić jeden błąd gdy jedno stanowisko niepoprawne")
    void shouldReportOneError_whenInvalidPosition() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("invalid-position.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POSITION,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,15000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(1, summary.getErrors().size(), "Powinien być 1 błąd");
    }
    
    /**
     * Test 2c: Niepoprawne stanowisko - treść błędu (nazwa stanowiska)
     */
    @Test
    @DisplayName("Błąd powinien zawierać nazwę niepoprawnego stanowiska")
    void shouldContainPositionName_inError() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("invalid-position.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POSITION,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,15000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertTrue(summary.getErrors().get(0).contains("INVALID_POSITION"), 
                   "Błąd powinien zawierać nazwę niepoprawnego stanowiska");
    }
    
    /**
     * Test 2d: Niepoprawne stanowisko - numer linii w błędzie
     */
    @Test
    @DisplayName("Błąd powinien wskazywać numer linii")
    void shouldContainLineNumber_inError() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("invalid-position.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POSITION,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,15000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertTrue(summary.getErrors().get(0).contains("Linia 3"), 
                   "Błąd powinien wskazywać numer linii");
    }
    
    /**
     * Test 2e: Niepoprawne stanowisko - rozmiar EmployeeService
     */
    @Test
    @DisplayName("EmployeeService powinien mieć tylko poprawnych pracowników")
    void shouldHaveOnlyValidEmployees_whenInvalidPosition() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("invalid-position.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POSITION,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,15000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(2, employeeService.size());
    }
    
    /**
     * Test 3a: Ujemne wynagrodzenie - liczba zaimportowanych
     */
    @Test
    @DisplayName("Powinien pominąć wiersz z ujemnym wynagrodzeniem - liczba")
    void shouldImportOnlyPositiveSalaries_count() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("negative-salary.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,-5000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,PREZES,25000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(2, summary.getImportedCount(), "Powinno zaimportować 2 poprawnych pracowników");
    }
    
    /**
     * Test 3b: Ujemne wynagrodzenie - liczba błędów
     */
    @Test
    @DisplayName("Powinien zgłosić błąd dla ujemnego wynagrodzenia")
    void shouldReportError_whenNegativeSalary() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("negative-salary.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,-5000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,PREZES,25000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(1, summary.getErrors().size(), "Powinien być 1 błąd");
    }
    
    /**
     * Test 3c: Ujemne wynagrodzenie - treść błędu
     */
    @Test
    @DisplayName("Błąd powinien wspominać o dodatnim wynagrodzeniu")
    void shouldMentionPositiveSalary_inError() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("negative-salary.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,-5000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,PREZES,25000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertTrue(summary.getErrors().get(0).contains("dodatnie"), 
                   "Błąd powinien wspominać o dodatnim wynagrodzeniu");
    }
    
    /**
     * Test 3d: Ujemne wynagrodzenie - rozmiar EmployeeService
     */
    @Test
    @DisplayName("EmployeeService nie powinien zawierać pracownika z ujemną pensją")
    void shouldExcludeNegativeSalary_fromService() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("negative-salary.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,-5000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,PREZES,25000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(2, employeeService.size());
    }
    
    /**
     * Test 4a: ImportSummary - nie może być null
     */
    @Test
    @DisplayName("Powinien zwrócić niepusty ImportSummary")
    void shouldReturnNonNullSummary() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("mixed-data.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POS,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,-100
                Maria,Kowalczyk,maria@test.com,TestCorp,WICEPREZES,16000
                Tomasz,Nowak,tomasz@test.com,TestCorp,PREZES,abc
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertNotNull(summary, "ImportSummary nie powinien być null");
    }
    
    /**
     * Test 4b: ImportSummary - liczba zaimportowanych
     */
    @Test
    @DisplayName("ImportSummary powinien zawierać poprawną liczbę zaimportowanych")
    void shouldHaveCorrectImportedCount_inSummary() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("mixed-data.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POS,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,-100
                Maria,Kowalczyk,maria@test.com,TestCorp,WICEPREZES,16000
                Tomasz,Nowak,tomasz@test.com,TestCorp,PREZES,abc
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(2, summary.getImportedCount(), "Powinno zaimportować 2 poprawnych pracowników");
    }
    
    /**
     * Test 4c: ImportSummary - liczba błędów
     */
    @Test
    @DisplayName("ImportSummary powinien zawierać poprawną liczbę błędów")
    void shouldHaveCorrectErrorCount_inSummary() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("mixed-data.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POS,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,-100
                Maria,Kowalczyk,maria@test.com,TestCorp,WICEPREZES,16000
                Tomasz,Nowak,tomasz@test.com,TestCorp,PREZES,abc
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(3, summary.getErrors().size(), "Powinny być 3 błędy");
    }
    
    /**
     * Test 4d: ImportSummary - lista błędów nie null
     */
    @Test
    @DisplayName("Lista błędów w ImportSummary nie powinna być null")
    void shouldHaveNonNullErrorsList_inSummary() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("mixed-data.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POS,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,-100
                Maria,Kowalczyk,maria@test.com,TestCorp,WICEPREZES,16000
                Tomasz,Nowak,tomasz@test.com,TestCorp,PREZES,abc
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertNotNull(summary.getErrors(), "Lista błędów nie powinna być null");
    }
    
    /**
     * Test 4e: ImportSummary - błąd niepoprawnej pozycji
     */
    @Test
    @DisplayName("Lista błędów powinna zawierać błąd niepoprawnej pozycji")
    void shouldContainInvalidPositionError_inSummary() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("mixed-data.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POS,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,-100
                Maria,Kowalczyk,maria@test.com,TestCorp,WICEPREZES,16000
                Tomasz,Nowak,tomasz@test.com,TestCorp,PREZES,abc
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        List<String> errors = summary.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.contains("INVALID_POS")));
    }
    
    /**
     * Test 4f: ImportSummary - błąd ujemnej pensji
     */
    @Test
    @DisplayName("Lista błędów powinna zawierać błąd ujemnej pensji")
    void shouldContainNegativeSalaryError_inSummary() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("mixed-data.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POS,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,-100
                Maria,Kowalczyk,maria@test.com,TestCorp,WICEPREZES,16000
                Tomasz,Nowak,tomasz@test.com,TestCorp,PREZES,abc
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        List<String> errors = summary.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.contains("dodatnie")));
    }
    
    /**
     * Test 4g: ImportSummary - błąd formatu liczby
     */
    @Test
    @DisplayName("Lista błędów powinna zawierać błąd formatu liczby")
    void shouldContainFormatError_inSummary() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("mixed-data.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,INVALID_POS,12000
                Piotr,Wiśniewski,piotr@cloudinc.com,CloudInc,MANAGER,-100
                Maria,Kowalczyk,maria@test.com,TestCorp,WICEPREZES,16000
                Tomasz,Nowak,tomasz@test.com,TestCorp,PREZES,abc
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        List<String> errors = summary.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.contains("format")));
    }
    
    /**
     * Test 5: Import pustego pliku
     */
    @Test
    @DisplayName("Powinien obsłużyć pusty plik CSV")
    void shouldHandleEmptyFile() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("empty.csv");
        String csvContent = "firstName,lastName,email,company,position,salary\n";
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(0, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
        assertEquals(0, employeeService.size());
    }
    
    /**
     * Test 6: Import pliku z pustymi liniami
     */
    @Test
    @DisplayName("Powinien zignorować puste linie w pliku CSV")
    void shouldIgnoreBlankLines() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("blank-lines.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                
                
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,12000
                
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(2, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
    }
    
    /**
     * Test 7: Import z niepoprawną liczbą kolumn
     */
    @Test
    @DisplayName("Powinien zgłosić błąd przy niepoprawnej liczbie kolumn")
    void shouldHandleMissingColumns() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("missing-columns.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER
                Piotr,Wiśniewski,piotr@test.com,CloudInc,PREZES,25000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(2, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).contains("liczba kolumn"));
    }
    
    /**
     * Test 8: Import z duplikowanymi emailami
     */
    @Test
    @DisplayName("Powinien zgłosić błąd przy duplikacie emaila")
    void shouldHandleDuplicateEmails() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("duplicate-emails.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,Nowak,JAN@TECHCORP.COM,DataSoft,MANAGER,12000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(1, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).toLowerCase().contains("istnieje") || 
                   summary.getErrors().get(0).toLowerCase().contains("duplicate"));
    }
    
    /**
     * Test 9: Import z wynagrodzeniem równym zero
     */
    @Test
    @DisplayName("Powinien pominąć wiersz z wynagrodzeniem równym zero")
    void shouldHandleZeroSalary() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("zero-salary.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,PROGRAMISTA,0
                Anna,Nowak,anna@datasoft.com,DataSoft,MANAGER,12000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(1, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).contains("dodatnie"));
    }
    
    /**
     * Test 10a: Case insensitive position - liczba zaimportowanych
     */
    @Test
    @DisplayName("Powinien zaimportować wszystkie stanowiska niezależnie od wielkości liter")
    void shouldImportAllPositions_caseInsensitive() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("position-case.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,programista,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,Manager,12000
                Piotr,Wiśniewski,piotr@test.com,CloudInc,PREZES,25000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(3, summary.getImportedCount());
    }
    
    /**
     * Test 10b: Case insensitive position - brak błędów
     */
    @Test
    @DisplayName("Nie powinno być błędów gdy stanowiska różnią się wielkością liter")
    void shouldHaveNoErrors_caseInsensitivePositions() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("position-case.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,programista,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,Manager,12000
                Piotr,Wiśniewski,piotr@test.com,CloudInc,PREZES,25000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertTrue(summary.getErrors().isEmpty());
    }
    
    /**
     * Test 10c: Case insensitive position - programista małymi literami
     */
    @Test
    @DisplayName("Powinien rozpoznać PROGRAMISTA napisane małymi literami")
    void shouldRecognizeProgramista_lowercase() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("position-case.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,programista,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,Manager,12000
                Piotr,Wiśniewski,piotr@test.com,CloudInc,PREZES,25000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        importService.importFromCsv(csvFile.toString());
        
        // Assert
        List<Employee> employees = employeeService.getAllEmployees();
        assertTrue(employees.stream().anyMatch(e -> e.getPosition() == Position.PROGRAMISTA));
    }
    
    /**
     * Test 10d: Case insensitive position - manager mieszanymi literami
     */
    @Test
    @DisplayName("Powinien rozpoznać MANAGER napisane mieszanymi literami")
    void shouldRecognizeManager_mixedCase() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("position-case.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,programista,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,Manager,12000
                Piotr,Wiśniewski,piotr@test.com,CloudInc,PREZES,25000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        importService.importFromCsv(csvFile.toString());
        
        // Assert
        List<Employee> employees = employeeService.getAllEmployees();
        assertTrue(employees.stream().anyMatch(e -> e.getPosition() == Position.MANAGER));
    }
    
    /**
     * Test 10e: Case insensitive position - prezes dużymi literami
     */
    @Test
    @DisplayName("Powinien rozpoznać PREZES napisane dużymi literami")
    void shouldRecognizePrezes_uppercase() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("position-case.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan@techcorp.com,TechCorp,programista,9000
                Anna,Nowak,anna@datasoft.com,DataSoft,Manager,12000
                Piotr,Wiśniewski,piotr@test.com,CloudInc,PREZES,25000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        importService.importFromCsv(csvFile.toString());
        
        // Assert
        List<Employee> employees = employeeService.getAllEmployees();
        assertTrue(employees.stream().anyMatch(e -> e.getPosition() == Position.PREZES));
    }
    
    /**
     * Test 11: Import z nazwami zawierającymi spacje
     */
    @Test
    @DisplayName("Powinien obsłużyć imiona i nazwiska z dodatkowymi spacjami")
    void shouldHandleNamesWithSpaces() throws IOException {
        // Arrange
        Path csvFile = tempDir.resolve("names-with-spaces.csv");
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan Maria,Kowalski-Nowak,jan@techcorp.com,TechCorp,PROGRAMISTA,9000
                Anna,de la Cruz,anna@datasoft.com,DataSoft,MANAGER,12000
                """;
        Files.writeString(csvFile, csvContent);
        
        // Act
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        
        // Assert
        assertEquals(2, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
        
        List<Employee> employees = employeeService.getAllEmployees();
        assertTrue(employees.stream().anyMatch(e -> e.getFullName().contains("Jan Maria")));
        assertTrue(employees.stream().anyMatch(e -> e.getFullName().contains("de la Cruz")));
    }
}
