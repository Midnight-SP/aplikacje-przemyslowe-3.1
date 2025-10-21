package com.techcorp.service;

import com.techcorp.exception.ApiException;
import com.techcorp.model.Employee;
import com.techcorp.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla klasy ApiService z użyciem mocków.
 * Testują komunikację z API bez wykonywania prawdziwych żądań HTTP.
 */
@ExtendWith(MockitoExtension.class)
class ApiServiceTest {
    
    @Mock
    private HttpClient httpClient;
    
    @Mock
    private HttpResponse<String> httpResponse;
    
    private ApiService apiService;
    
    /**
     * Przykładowa poprawna odpowiedź JSON z API
     */
    private static final String VALID_JSON_RESPONSE = """
            [
                {
                    "name": "Jan Kowalski",
                    "email": "jan@test.com",
                    "company": {
                        "name": "TechCorp"
                    }
                },
                {
                    "name": "Anna Nowak",
                    "email": "anna@test.com",
                    "company": {
                        "name": "DataSoft"
                    }
                }
            ]
            """;
    
    @BeforeEach
    void setUp() {
        // Tworzymy ApiService który będzie używał mockowanego HttpClient
        // Nie możemy wstrzyknąć mocka bezpośrednio, więc użyjemy refleksji
        // lub stworzymy konstruktor w ApiService przyjmujący HttpClient
        apiService = new ApiService();
    }
    
    /**
     * Test 1a: Poprawna odpowiedź - lista nie null
     */
    @Test
    @DisplayName("Powinien zwrócić niepustą listę gdy API zwraca poprawny JSON")
    void shouldReturnNonNullList_whenValidResponse() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        assertNotNull(employees);
    }
    
    /**
     * Test 1b: Poprawna odpowiedź - liczba pracowników
     */
    @Test
    @DisplayName("Powinien zwrócić właściwą liczbę pracowników")
    void shouldReturnCorrectCount_whenValidResponse() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        assertEquals(2, employees.size());
    }
    
    /**
     * Test 1c: Poprawna odpowiedź - imię pierwszego pracownika
     */
    @Test
    @DisplayName("Powinien poprawnie sparsować imię pierwszego pracownika")
    void shouldParseFirstEmployeeName_whenValidResponse() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        Employee first = employees.get(0);
        assertEquals("Jan Kowalski", first.getFullName());
    }
    
    /**
     * Test 1d: Poprawna odpowiedź - email pierwszego pracownika
     */
    @Test
    @DisplayName("Powinien poprawnie sparsować email pierwszego pracownika")
    void shouldParseFirstEmployeeEmail_whenValidResponse() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        Employee first = employees.get(0);
        assertEquals("jan@test.com", first.getEmail());
    }
    
    /**
     * Test 1e: Poprawna odpowiedź - firma pierwszego pracownika
     */
    @Test
    @DisplayName("Powinien poprawnie sparsować firmę pierwszego pracownika")
    void shouldParseFirstEmployeeCompany_whenValidResponse() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        Employee first = employees.get(0);
        assertEquals("TechCorp", first.getCompanyName());
    }
    
    /**
     * Test 1f: Poprawna odpowiedź - pozycja pierwszego pracownika
     */
    @Test
    @DisplayName("Powinien ustawić domyślną pozycję dla pierwszego pracownika")
    void shouldSetDefaultPosition_whenValidResponse() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        Employee first = employees.get(0);
        assertEquals(Position.PROGRAMISTA, first.getPosition());
    }
    
    /**
     * Test 1g: Poprawna odpowiedź - pensja pierwszego pracownika
     */
    @Test
    @DisplayName("Powinien ustawić domyślną pensję dla pierwszego pracownika")
    void shouldSetDefaultSalary_whenValidResponse() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        Employee first = employees.get(0);
        assertEquals(8000.0, first.getSalary());
    }
    
    /**
     * Test 1h: Poprawna odpowiedź - drugi pracownik
     */
    @Test
    @DisplayName("Powinien poprawnie sparsować imię drugiego pracownika")
    void shouldParseSecondEmployeeName_whenValidResponse() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        Employee second = employees.get(1);
        assertEquals("Anna Nowak", second.getFullName());
    }
    
    /**
     * Test 1i: Poprawna odpowiedź - email drugiego pracownika
     */
    @Test
    @DisplayName("Powinien poprawnie sparsować email drugiego pracownika")
    void shouldParseSecondEmployeeEmail_whenValidResponse() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        Employee second = employees.get(1);
        assertEquals("anna@test.com", second.getEmail());
    }
    
    /**
     * Test 1j: Poprawna odpowiedź - firma drugiego pracownika
     */
    @Test
    @DisplayName("Powinien poprawnie sparsować firmę drugiego pracownika")
    void shouldParseSecondEmployeeCompany_whenValidResponse() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        Employee second = employees.get(1);
        assertEquals("DataSoft", second.getCompanyName());
    }
    
    /**
     * Test 1k: Poprawna odpowiedź - weryfikacja wywołania HttpClient
     */
    @Test
    @DisplayName("Powinien wywołać HttpClient dokładnie raz")
    void shouldCallHttpClientOnce_whenValidResponse() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
    
    /**
     * Test 2a: Błąd HTTP 404 - rzucenie wyjątku
     */
    @Test
    @DisplayName("Powinien rzucić ApiException gdy otrzyma błąd 404")
    void shouldThrowException_when404() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(404);
        when(httpResponse.body()).thenReturn("Not Found");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act & Assert
        assertThrows(
            ApiException.class,
            () -> mockApiService.fetchEmployeesFromApi("https://api.example.com/users")
        );
    }
    
    /**
     * Test 2b: Błąd HTTP 404 - treść komunikatu
     */
    @Test
    @DisplayName("Wyjątek powinien zawierać kod 404 w komunikacie")
    void shouldContain404InMessage_whenError404() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(404);
        when(httpResponse.body()).thenReturn("Not Found");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        ApiException exception = assertThrows(
            ApiException.class,
            () -> mockApiService.fetchEmployeesFromApi("https://api.example.com/users")
        );
        
        // Assert
        assertTrue(exception.getMessage().contains("404"));
    }
    
    /**
     * Test 2c: Błąd HTTP 404 - weryfikacja wywołania
     */
    @Test
    @DisplayName("Powinien wywołać HttpClient raz mimo błędu 404")
    void shouldCallHttpClientOnce_when404() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(404);
        when(httpResponse.body()).thenReturn("Not Found");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        try {
            mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        } catch (ApiException e) {
            // Expected
        }
        
        // Assert
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
    
    /**
     * Test 3a: Błąd HTTP 500 - rzucenie wyjątku
     */
    @Test
    @DisplayName("Powinien rzucić ApiException gdy otrzyma błąd 500")
    void shouldThrowException_when500() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpResponse.body()).thenReturn("Internal Server Error");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act & Assert
        assertThrows(
            ApiException.class,
            () -> mockApiService.fetchEmployeesFromApi("https://api.example.com/users")
        );
    }
    
    /**
     * Test 3b: Błąd HTTP 500 - treść komunikatu
     */
    @Test
    @DisplayName("Wyjątek powinien zawierać kod 500 w komunikacie")
    void shouldContain500InMessage_whenError500() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpResponse.body()).thenReturn("Internal Server Error");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        ApiException exception = assertThrows(
            ApiException.class,
            () -> mockApiService.fetchEmployeesFromApi("https://api.example.com/users")
        );
        
        // Assert
        assertTrue(exception.getMessage().contains("500"));
    }
    
    /**
     * Test 3c: Błąd HTTP 500 - weryfikacja wywołania
     */
    @Test
    @DisplayName("Powinien wywołać HttpClient raz mimo błędu 500")
    void shouldCallHttpClientOnce_when500() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpResponse.body()).thenReturn("Internal Server Error");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        try {
            mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        } catch (ApiException e) {
            // Expected
        }
        
        // Assert
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
    
    /**
     * Test 4: IOException podczas komunikacji
     */
    @Test
    @DisplayName("Powinien rzucić ApiException gdy wystąpi IOException")
    void shouldThrowException_whenIOException() throws Exception {
        // Arrange
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection timeout"));
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act & Assert
        ApiException exception = assertThrows(
            ApiException.class,
            () -> mockApiService.fetchEmployeesFromApi("https://api.example.com/users")
        );
        
        assertTrue(exception.getMessage().contains("komunikacji"));
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
    }
    
    /**
     * Test 5: InterruptedException podczas komunikacji
     */
    @Test
    @DisplayName("Powinien rzucić ApiException gdy wątek zostanie przerwany")
    void shouldThrowException_whenInterrupted() throws Exception {
        // Arrange
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("Thread interrupted"));
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act & Assert
        ApiException exception = assertThrows(
            ApiException.class,
            () -> mockApiService.fetchEmployeesFromApi("https://api.example.com/users")
        );
        
        assertTrue(exception.getMessage().contains("Przerwano"));
        // Sprawdź czy flaga interrupted została ustawiona
        // W prawdziwej implementacji wątek powinien mieć ustawioną flagę przerwania
    }
    
    /**
     * Test 6: Parsowanie JSON z jednym użytkownikiem
     */
    @Test
    @DisplayName("Powinien poprawnie sparsować JSON z jednym użytkownikiem")
    void shouldParseJsonCorrectly_singleUser() throws Exception {
        // Arrange
        String singleUserJson = """
                [
                    {
                        "name": "Piotr Wiśniewski",
                        "email": "piotr@example.com",
                        "company": {
                            "name": "CloudInc"
                        }
                    }
                ]
                """;
        
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(singleUserJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        assertEquals(1, employees.size());
        Employee employee = employees.get(0);
        assertEquals("Piotr Wiśniewski", employee.getFullName());
        assertEquals("piotr@example.com", employee.getEmail());
        assertEquals("CloudInc", employee.getCompanyName());
    }
    
    /**
     * Test 7: Parsowanie JSON z niepoprawną strukturą
     */
    @Test
    @DisplayName("Powinien rzucić ApiException gdy JSON jest niepoprawny")
    void shouldThrowException_whenInvalidJson() throws Exception {
        // Arrange
        String invalidJson = "{ invalid json structure }";
        
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(invalidJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act & Assert
        ApiException exception = assertThrows(
            ApiException.class,
            () -> mockApiService.fetchEmployeesFromApi("https://api.example.com/users")
        );
        
        assertTrue(exception.getMessage().contains("parsowania") || 
                   exception.getMessage().contains("JSON"));
    }
    
    /**
     * Test 8: Parsowanie użytkownika z imieniem składającym się z jednego słowa
     */
    @Test
    @DisplayName("Powinien obsłużyć imię składające się z jednego słowa")
    void shouldHandleSingleWordName() throws Exception {
        // Arrange
        String singleNameJson = """
                [
                    {
                        "name": "Madonna",
                        "email": "madonna@example.com",
                        "company": {
                            "name": "MusicCorp"
                        }
                    }
                ]
                """;
        
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(singleNameJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        assertEquals(1, employees.size());
        assertEquals("Madonna", employees.get(0).getFullName());
    }
    
    /**
     * Test 9: Weryfikacja domyślnych wartości (Position i Salary)
     */
    @Test
    @DisplayName("Powinien ustawić domyślne wartości dla pozycji i wynagrodzenia")
    void shouldSetDefaultPositionAndSalary() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_JSON_RESPONSE);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        for (Employee employee : employees) {
            assertEquals(Position.PROGRAMISTA, employee.getPosition(), 
                        "Domyślna pozycja powinna być PROGRAMISTA");
            assertEquals(Position.PROGRAMISTA.getBaseSalary(), employee.getSalary(), 
                        "Domyślne wynagrodzenie powinno być równe bazowej stawce dla pozycji");
        }
    }
    
    /**
     * Test 10: Pusta odpowiedź JSON (pusta tablica)
     */
    @Test
    @DisplayName("Powinien obsłużyć pustą tablicę JSON")
    void shouldHandleEmptyJsonArray() throws Exception {
        // Arrange
        String emptyJson = "[]";
        
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(emptyJson);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        
        ApiService mockApiService = createApiServiceWithMockedClient(httpClient);
        
        // Act
        List<Employee> employees = mockApiService.fetchEmployeesFromApi("https://api.example.com/users");
        
        // Assert
        assertNotNull(employees);
        assertTrue(employees.isEmpty());
    }
    
    /**
     * Metoda pomocnicza do tworzenia ApiService z mockowanym HttpClient.
     * 
     * UWAGA: Ta metoda wymaga aby ApiService miał konstruktor przyjmujący HttpClient:
     * 
     * public ApiService(HttpClient httpClient) {
     *     this.httpClient = httpClient;
     *     this.gson = new Gson();
     * }
     * 
     * Alternatywnie można użyć refleksji do wstrzyknięcia mocka.
     */
    private ApiService createApiServiceWithMockedClient(HttpClient mockClient) {
        try {
            // Próba użycia konstruktora z HttpClient
            var constructor = ApiService.class.getConstructor(HttpClient.class);
            return constructor.newInstance(mockClient);
        } catch (NoSuchMethodException e) {
            // Jeśli konstruktor nie istnieje, użyj refleksji
            try {
                ApiService service = new ApiService();
                var field = ApiService.class.getDeclaredField("httpClient");
                field.setAccessible(true);
                field.set(service, mockClient);
                return service;
            } catch (Exception ex) {
                throw new RuntimeException("Nie można wstrzyknąć mockowanego HttpClient. " +
                        "Dodaj konstruktor: public ApiService(HttpClient httpClient)", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas tworzenia ApiService z mockiem", e);
        }
    }
}
