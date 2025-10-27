package com.techcorp.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.techcorp.model.Employee;
import com.techcorp.model.Position;
import com.techcorp.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApiService {
    private final HttpClient httpClient;
    private final Gson gson;

    @Value("${app.api.url:}")
    private String defaultApiUrl;

    @Autowired
    public ApiService(HttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    public ApiService() {
        this(HttpClient.newHttpClient(), new Gson());
    }

    public ApiService(HttpClient httpClient) {
        this(httpClient, new Gson());
    }
    
    public List<Employee> fetchEmployeesFromApi(String apiUrl) throws ApiException {
    try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .header("Accept", "application/json")
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new ApiException("Błąd HTTP: " + response.statusCode() + " - " + response.body());
            }
            
            return parseJsonResponse(response.body());
            
        } catch (IOException e) {
            throw new ApiException("Błąd podczas komunikacji z API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Przerwano połączenie z API: " + e.getMessage(), e);
        } catch (JsonSyntaxException e) {
            throw new ApiException("Błąd parsowania JSON: " + e.getMessage(), e);
        }
    }

    public List<Employee> fetchEmployeesFromApi() throws ApiException {
        if (defaultApiUrl == null || defaultApiUrl.isBlank()) {
            throw new ApiException("Brak skonfigurowanego app.api.url w application.properties");
        }
        return fetchEmployeesFromApi(defaultApiUrl);
    }
    
    private List<Employee> parseJsonResponse(String jsonResponse) throws ApiException {
        List<Employee> employees = new ArrayList<>();
        
        try {
            JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);
            
            for (JsonElement element : jsonArray) {
                JsonObject userObject = element.getAsJsonObject();
                
                String fullName = userObject.get("name").getAsString();
                
                String[] nameParts = fullName.trim().split("\\s+", 2);
                String firstName = nameParts[0];
                String lastName = nameParts.length > 1 ? nameParts[1] : "";
                String employeeFullName = firstName + (lastName.isEmpty() ? "" : " " + lastName);
                
                String email = userObject.get("email").getAsString();
                
                JsonObject companyObject = userObject.get("company").getAsJsonObject();
                String companyName = companyObject.get("name").getAsString();
                
                Position position = Position.PROGRAMISTA;
                double salary = position.getBaseSalary();
                
                Employee employee = new Employee(employeeFullName, email, companyName, position, salary);
                employees.add(employee);
            }
            
        } catch (Exception e) {
            throw new ApiException("Błąd podczas parsowania danych użytkownika: " + e.getMessage(), e);
        }
        
        return employees;
    }
}
