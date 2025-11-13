package com.techcorp.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.techcorp.model.Employee;
import com.techcorp.model.EmploymentStatus;
import com.techcorp.model.Position;
import com.techcorp.service.EmployeeService;

@WebMvcTest(controllers = StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void shouldGetAverageSalary() throws Exception {
        when(employeeService.getAverageSalary()).thenReturn(java.util.OptionalDouble.of(10000.0));

        mockMvc.perform(get("/api/statistics/salary/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary", is(10000.0)));

        verify(employeeService, times(1)).getAverageSalary();
    }

    @Test
    void shouldGetAverageSalaryByCompany() throws Exception {
        Employee emp1 = new Employee("Jan Kowalski", "jan@test.com", "TechCorp", Position.PROGRAMISTA, 8000);
        Employee emp2 = new Employee("Anna Nowak", "anna@test.com", "TechCorp", Position.MANAGER, 12000);
        List<Employee> employees = Arrays.asList(emp1, emp2);

        when(employeeService.findByCompany("TechCorp")).thenReturn(employees);

        mockMvc.perform(get("/api/statistics/salary/average")
                        .param("company", "TechCorp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageSalary", is(10000.0)));

        verify(employeeService, times(1)).findByCompany("TechCorp");
    }

    @Test
    void shouldGetPositionStatistics() throws Exception {
        Map<Position, Long> stats = new HashMap<>();
        stats.put(Position.PROGRAMISTA, 5L);
        stats.put(Position.MANAGER, 2L);

        when(employeeService.countByPosition()).thenReturn(stats);

        mockMvc.perform(get("/api/statistics/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PROGRAMISTA", is(5)))
                .andExpect(jsonPath("$.MANAGER", is(2)));

        verify(employeeService, times(1)).countByPosition();
    }

    @Test
    void shouldGetStatusStatistics() throws Exception {
        Map<EmploymentStatus, Long> stats = new HashMap<>();
        stats.put(EmploymentStatus.ACTIVE, 10L);
        stats.put(EmploymentStatus.ON_LEAVE, 2L);

        when(employeeService.countByStatus()).thenReturn(stats);

        mockMvc.perform(get("/api/statistics/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ACTIVE", is(10)))
                .andExpect(jsonPath("$.ON_LEAVE", is(2)));

        verify(employeeService, times(1)).countByStatus();
    }
}
