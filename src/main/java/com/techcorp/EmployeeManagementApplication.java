package com.techcorp;

import com.techcorp.model.Employee;
import com.techcorp.model.CompanyStatistics;
import com.techcorp.service.ApiService;
import com.techcorp.service.EmployeeService;
import com.techcorp.service.ImportService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@ImportResource("classpath:employees-beans.xml")
public class EmployeeManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner dataLoader(
            EmployeeService employeeService,
            ImportService importService,
            ApiService apiService,
            @Qualifier("xmlEmployees") List<Employee> xmlEmployees,
            @Value("${app.import.csv-file}") String csvPath
    ) {
        return args -> {
            p("\n=== TechCorp Employee Management (Spring Boot) ===\n");

            p("1. Import CSV: " + csvPath);
            String effectiveCsvPath = csvPath;
            try {
                var url = Thread.currentThread().getContextClassLoader().getResource(csvPath);
                if (url != null) {
                    effectiveCsvPath = java.nio.file.Paths.get(url.toURI()).toString();
                }
            } catch (Exception ignore) {
            }
            var summary = importService.importFromCsv(effectiveCsvPath);
            p("   - Zaimportowano: " + summary.getImportedCount());
            if (!summary.getErrors().isEmpty()) {
                p("   - Bledy: ");
                summary.getErrors().forEach(e -> p("     * " + e));
            }

            p("\n2. Dodawanie pracownikow z XML (" + xmlEmployees.size() + ")...");
            for (Employee e : xmlEmployees) {
                try {
                    employeeService.addEmployee(e);
                } catch (Exception ex) {
                    p("   - Pominieto: " + e.getEmail() + " (" + ex.getMessage() + ")");
                }
            }

            p("\n3. Pobieranie pracownikow z API...");
            var apiEmployees = apiService.fetchEmployeesFromApi();
            p("   - Pobrano z API: " + apiEmployees.size());
            int added = 0;
            for (Employee e : apiEmployees) {
                try {
                    employeeService.addEmployee(e);
                    added++;
                } catch (Exception ex) {
                    p("   - Pominieto: " + e.getEmail() + " (" + ex.getMessage() + ")");
                }
            }
            System.out.println("   - Dodano do systemu: " + added);

            System.out.println("\n4. Statystyki firm:");
            Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
            stats.forEach((company, s) -> {
                p(" - " + company + ":");
                p("    * Liczba pracowników: " + s.getEmployeeCount());
                pf("    * Średnia pensja: %.2f%n", s.getAverageSalary());
                p("    * Najlepiej opłacany: " + s.getHighestPaidEmployee());
            });

            p("\n5. Walidacja wynagrodzen wzgledem stawek stanowisk:");
            var underpaid = employeeService.validateSalaryConsistency();
            if (underpaid.isEmpty()) {
                System.out.println("   - Wszystko OK");
            } else {
                p("   - Ponizej bazowej stawki:");
                underpaid.forEach(emp -> pf(
                        "     * %s (%s): %.2f PLN vs %.2f PLN%n",
                        emp.getFullName(),
                        emp.getPosition(),
                        emp.getSalary(),
                        emp.getPosition().getBaseSalary()
                ));
            }
            System.out.println();
        };
    }

    private static String ascii(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        n = n.replace("ł", "l").replace("Ł", "L");
        return n;
    }

    private static void p(String s) {
        System.out.println(ascii(s));
    }

    private static void pf(String format, Object... args) {
        System.out.print(ascii(String.format(format, args)));
    }
}