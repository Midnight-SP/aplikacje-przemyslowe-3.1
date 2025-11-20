package com.techcorp.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.techcorp.model.DocumentType;
import com.techcorp.model.EmployeeDocument;
import com.techcorp.service.FileStorageService;
import com.techcorp.service.ReportGeneratorService;
import com.techcorp.service.EmployeeService;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService storageService;
    private final ReportGeneratorService reportService;
    private final EmployeeService employeeService;
    private final com.techcorp.service.ImportService importService;

    public FileUploadController(FileStorageService storageService,
                                ReportGeneratorService reportService,
                                EmployeeService employeeService,
                                com.techcorp.service.ImportService importService) {
        this.storageService = storageService;
        this.reportService = reportService;
        this.employeeService = employeeService;
        this.importService = importService;
    }

    @PostMapping("/import/csv")
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file) {
        String stored = storageService.storeFile(file, "imports");
        com.techcorp.model.ImportSummary summary = importService.importFromCsv(stored);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/import/xml")
    public ResponseEntity<?> importXml(@RequestParam("file") MultipartFile file) {
        String stored = storageService.storeFile(file, "imports");
        com.techcorp.model.ImportSummary summary = importService.importFromXml(stored);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/documents/{email}")
    public ResponseEntity<EmployeeDocument> uploadDocument(@PathVariable String email,
                                                           @RequestParam("file") MultipartFile file,
                                                           @RequestParam(name = "type", required = false, defaultValue = "OTHER") DocumentType type) {
        String subPath = "documents/" + email.toLowerCase();
        String stored = storageService.storeFile(file, subPath);
        EmployeeDocument doc = storageService.registerDocument(email, stored, file.getOriginalFilename(), type);
        return ResponseEntity.status(201).body(doc);
    }

    @GetMapping("/documents/{email}")
    public ResponseEntity<List<EmployeeDocument>> listDocuments(@PathVariable String email) {
        return ResponseEntity.ok(storageService.listDocuments(email));
    }

    @GetMapping("/documents/{email}/{documentId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String email, @PathVariable String documentId) {
        EmployeeDocument doc = storageService.getDocument(email, documentId);
        Resource resource = storageService.loadAsResource(doc.getFilePath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getOriginalFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/documents/{email}/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String email, @PathVariable String documentId) {
        storageService.deleteDocument(email, documentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/photos/{email}")
    public ResponseEntity<?> uploadPhoto(@PathVariable String email, @RequestParam("file") MultipartFile file) {
        String stored = storageService.storePhoto(file, email);
        // set on employee if exists
        try {
            var empOpt = employeeService.findByEmail(email);
            empOpt.ifPresent(emp -> emp.setPhotoFileName(java.nio.file.Paths.get(stored).getFileName().toString()));
        } catch (Exception e) {
            // ignore silently
        }
        return ResponseEntity.status(201).body("Photo uploaded");
    }

    @GetMapping("/photos/{email}")
    public ResponseEntity<Resource> downloadPhoto(@PathVariable String email) {
        var docs = storageService.listDocuments(email);
        // try to find photo in uploads/photos
        Path photoDir = storageService.getReportsDir().getParent().resolveSibling("uploads").resolve("photos").resolve(email.toLowerCase());
        // fallback: check employee record
        var emp = employeeService.findByEmail(email);
        if (emp.isPresent() && emp.get().getPhotoFileName() != null) {
            String filePath = storageService.getReportsDir().getParent().resolveSibling("uploads").resolve("photos").resolve(email.toLowerCase()).resolve(emp.get().getPhotoFileName()).toString();
            Resource resource = storageService.loadAsResource(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        }
        throw new com.techcorp.exception.FileMissingException("Photo not found for " + email);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportAllCsv(@RequestParam(name = "company", required = false) String company) {
        Resource res;
        if (company == null || company.isBlank()) {
            res = reportService.generateCsv(employeeService.getAllEmployees(), "employees_export.csv");
        } else {
            res = reportService.generateCsv(employeeService.findByCompany(company), "employees_export_" + company + ".csv");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(res);
    }

    @GetMapping("/reports/statistics/{companyName}")
    public ResponseEntity<Resource> exportCompanyPdf(@PathVariable String companyName) {
        Resource res = reportService.generatePdfForCompany(companyName, "company_report_" + companyName + ".pdf");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(res);
    }
}
