package com.techcorp.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techcorp.dto.EmployeeDTO;
import com.techcorp.exception.DuplicateEmailException;
import com.techcorp.exception.EmployeeNotFoundException;
import com.techcorp.model.Employee;
import com.techcorp.model.EmploymentStatus;
import com.techcorp.model.Position;
import com.techcorp.service.EmployeeService;
import com.techcorp.service.ImportService;
import com.techcorp.service.ApiService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = EmployeeController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.main.lazy-initialization=true",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private ImportService importService;

    @MockBean
    private ApiService apiService;

    @MockBean(name = "xmlEmployees")
    private List<Employee> xmlEmployees;

    @Test
    void shouldGetAllEmployees() throws Exception {
        Employee emp1 = new Employee("Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000);
        List<Employee> employees = Arrays.asList(emp1, emp2);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email", is("jan@test.com")))
                .andExpect(jsonPath("$[1].email", is("anna@test.com")));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void shouldGetEmployeeByEmail() throws Exception {
        Employee emp = new Employee("Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 8000);

        when(employeeService.getByEmail("jan@test.com")).thenReturn(emp);

        mockMvc.perform(get("/api/employees/jan@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("jan@test.com")))
                .andExpect(jsonPath("$.firstName", is("Jan")))
                .andExpect(jsonPath("$.lastName", is("Kowalski")))
                .andExpect(jsonPath("$.company", is("TechCorp")))
                .andExpect(jsonPath("$.position", is("PROGRAMISTA")))
                .andExpect(jsonPath("$.salary", is(8000.0)));

        verify(employeeService, times(1)).getByEmail("jan@test.com");
    }

    @Test
    void shouldReturn404WhenEmployeeNotFound() throws Exception {
        when(employeeService.getByEmail("nonexistent@test.com"))
                .thenThrow(new EmployeeNotFoundException("nonexistent@test.com"));

        mockMvc.perform(get("/api/employees/nonexistent@test.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Employee not found")));

        verify(employeeService, times(1)).getByEmail("nonexistent@test.com");
    }

    @Test
    void shouldCreateEmployee() throws Exception {
        EmployeeDTO dto = new EmployeeDTO("Jan", "Kowalski", "jan@test.com", 
                "TechCorp", Position.PROGRAMISTA, 8000, EmploymentStatus.ACTIVE);

        doNothing().when(employeeService).addEmployee(any(Employee.class));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/employees/jan@test.com"))
                .andExpect(jsonPath("$.email", is("jan@test.com")));

        verify(employeeService, times(1)).addEmployee(any(Employee.class));
    }

    @Test
    void shouldReturn409WhenCreatingDuplicateEmployee() throws Exception {
        EmployeeDTO dto = new EmployeeDTO("Jan", "Kowalski", "jan@test.com", 
                "TechCorp", Position.PROGRAMISTA, 8000, EmploymentStatus.ACTIVE);

        doThrow(new DuplicateEmailException("jan@test.com"))
                .when(employeeService).addEmployee(any(Employee.class));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));

        verify(employeeService, times(1)).addEmployee(any(Employee.class));
    }

    @Test
    void shouldDeleteEmployee() throws Exception {
        doNothing().when(employeeService).deleteEmployee("jan@test.com");

        mockMvc.perform(delete("/api/employees/jan@test.com"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee("jan@test.com");
    }

    @Test
    void shouldFilterEmployeesByCompany() throws Exception {
        Employee emp1 = new Employee("Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 8000);
        List<Employee> employees = Arrays.asList(emp1);

        when(employeeService.findByCompany("TechCorp")).thenReturn(employees);

        mockMvc.perform(get("/api/employees")
                        .param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].company", is("TechCorp")));

        verify(employeeService, times(1)).findByCompany("TechCorp");
    }

    @Test
    void shouldUpdateEmployeeStatus() throws Exception {
        Employee emp = new Employee("Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 8000);
        emp.setStatus(EmploymentStatus.ON_LEAVE);

        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "ON_LEAVE");

        doNothing().when(employeeService).updateEmployeeStatus(eq("jan@test.com"), eq(EmploymentStatus.ON_LEAVE));
        when(employeeService.getByEmail("jan@test.com")).thenReturn(emp);

        mockMvc.perform(patch("/api/employees/jan@test.com/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ON_LEAVE")));

        verify(employeeService, times(1)).updateEmployeeStatus("jan@test.com", EmploymentStatus.ON_LEAVE);
    }

    @Test
    void shouldGetEmployeesByStatus() throws Exception {
        Employee emp1 = new Employee("Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 8000);
        emp1.setStatus(EmploymentStatus.ACTIVE);
        List<Employee> employees = Arrays.asList(emp1);

        when(employeeService.findByStatus(EmploymentStatus.ACTIVE)).thenReturn(employees);

        mockMvc.perform(get("/api/employees/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));

        verify(employeeService, times(1)).findByStatus(EmploymentStatus.ACTIVE);
    }
}
