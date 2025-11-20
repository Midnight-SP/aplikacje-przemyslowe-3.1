package com.techcorp.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import com.techcorp.model.Employee;

@Service
public class ReportGeneratorService {
    private final FileStorageService storageService;
    private final EmployeeService employeeService;

    public ReportGeneratorService(FileStorageService storageService, EmployeeService employeeService) {
        this.storageService = storageService;
        this.employeeService = employeeService;
    }

    public Resource generateCsv(List<Employee> employees, String fileName) {
        try {
            Path out = storageService.getReportsDir().resolve(fileName).toAbsolutePath().normalize();
            try (BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
                writer.write("fullName,email,company,position,salary,status");
                writer.newLine();
                for (Employee e : employees) {
                    writer.write(String.format("%s,%s,%s,%s,%.2f,%s",
                            e.getFullName(), e.getEmail(), e.getCompanyName(), e.getPosition(), e.getSalary(), e.getStatus()));
                    writer.newLine();
                }
            }
            return new UrlResource(out.toUri());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to generate CSV report", ex);
        }
    }

    public Resource generateCsv(Map<String, List<Employee>> grouped, String fileName) {
        List<Employee> list = grouped.values().stream().flatMap(List::stream).collect(Collectors.toList());
        return generateCsv(list, fileName);
    }

    public Resource generatePdfForCompany(String companyName, String fileName) {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);

            List<Employee> list = employeeService.findByCompany(companyName);
            double total = list.stream().mapToDouble(Employee::getSalary).sum();
            int count = list.size();

            try (org.apache.pdfbox.pdmodel.PDPageContentStream contents = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
                org.apache.pdfbox.pdmodel.font.PDFont fontBold = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD;
                org.apache.pdfbox.pdmodel.font.PDFont font = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;
                float y = 760f;

                // Header
                contents.beginText();
                contents.setFont(fontBold, 16);
                contents.newLineAtOffset(50, y);
                contents.showText("Company report: " + companyName);
                contents.endText();

                // Stats
                contents.beginText();
                contents.setFont(font, 12);
                contents.newLineAtOffset(50, y - 30);
                contents.showText("Employees: " + count + "    Total salary: " + String.format("%.2f", total));
                contents.endText();

                // List
                float startY = y - 60f;
                contents.beginText();
                contents.setFont(font, 11);
                contents.newLineAtOffset(50, startY);
                for (Employee e : list) {
                    String line = String.format("%s (%s) - %.2f", e.getFullName(), e.getEmail(), e.getSalary());
                    contents.showText(line);
                    contents.newLineAtOffset(0, -14);
                }
                contents.endText();
            }

            java.nio.file.Path out = storageService.getReportsDir().resolve(fileName).toAbsolutePath().normalize();
            doc.save(out.toFile());
            return new UrlResource(out.toUri());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to generate PDF report", ex);
        }
    }
}
