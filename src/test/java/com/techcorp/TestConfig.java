package com.techcorp;

import com.techcorp.model.Employee;
import com.techcorp.service.ApiService;
import com.techcorp.service.EmployeeService;
import com.techcorp.service.ImportService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Collections;
import java.util.List;

@TestConfiguration
public class TestConfig {

    @Bean(name = "xmlEmployees")
    @Primary
    public List<Employee> xmlEmployees() {
        return Collections.emptyList();
    }
}
