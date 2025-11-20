package com.techcorp.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import com.techcorp.model.Employee;
import com.techcorp.model.Position;

public class ReportGeneratorServiceTest {

    @TempDir
    Path tempDir;

    @Test
    public void generatePdfForCompany_containsStatsAndEmployees() throws Exception {
        FileStorageService storageService = Mockito.mock(FileStorageService.class);
        EmployeeService employeeService = Mockito.mock(EmployeeService.class);

        Mockito.when(storageService.getReportsDir()).thenReturn(tempDir);

        Employee e1 = new Employee("Alice Smith", "alice@acme.com", "ACME", Position.PROGRAMISTA, 5000.0);
        Employee e2 = new Employee("Bob Jones", "bob@acme.com", "ACME", Position.MANAGER, 8000.0);

        Mockito.when(employeeService.findByCompany("ACME")).thenReturn(List.of(e1, e2));

        ReportGeneratorService svc = new ReportGeneratorService(storageService, employeeService);
        var res = svc.generatePdfForCompany("ACME", "acme_report_test.pdf");

        Path pdf = tempDir.resolve("acme_report_test.pdf");
        assertThat(Files.exists(pdf)).isTrue();

        try (PDDocument doc = PDDocument.load(pdf.toFile())) {
            String text = new PDFTextStripper().getText(doc);
            assertThat(text).contains("Company report: ACME");
            assertThat(text).contains("Employees: 2");
            assertThat(text).contains("Alice Smith");
            assertThat(text).contains("Bob Jones");
            assertThat(text).contains("13000.00");
        }
    }
}
