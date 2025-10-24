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

@ExtendWith(MockitoExtension.class)
class ApiServiceTest {
    
    @Mock
    private HttpClient httpClient;
    
    @Mock
    private HttpResponse<String> httpResponse;
    
    private ApiService apiService;
    
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
        apiService = new ApiService();
    }
    
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
