package com.techcorp.controller;

import com.techcorp.model.ImportSummary;
import com.techcorp.service.FileStorageService;
import com.techcorp.service.ImportService;
import com.techcorp.service.ReportGeneratorService;
import com.techcorp.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import org.springframework.core.io.ByteArrayResource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileUploadController.class)
@org.springframework.test.context.ActiveProfiles("test")
public class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService storageService;

    @MockBean
    private ImportService importService;

    @MockBean
    private ReportGeneratorService reportService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private com.techcorp.service.ApiService apiService;

    @Test
    public void importCsv_shouldReturnImportSummary() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "employees.csv",
                MediaType.TEXT_PLAIN_VALUE, "first,second".getBytes());

        given(storageService.storeFile(any(), eq("imports"))).willReturn("/tmp/uploads/imports/employees.csv");
        given(importService.importFromCsv("/tmp/uploads/imports/employees.csv")).willReturn(new ImportSummary(2, Collections.emptyList()));

        mockMvc.perform(multipart("/api/files/import/csv").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(2));
    }

    @Test
    public void importXml_shouldReturnImportSummary() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "employees.xml",
        MediaType.APPLICATION_XML_VALUE, "<employees></employees>".getBytes());

    given(storageService.storeFile(any(), eq("imports"))).willReturn("/tmp/uploads/imports/employees.xml");
    given(importService.importFromXml("/tmp/uploads/imports/employees.xml")).willReturn(new ImportSummary(1, Collections.emptyList()));

    mockMvc.perform(multipart("/api/files/import/xml").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.importedCount").value(1));
    }

    @Test
    public void uploadDocument_shouldReturn201() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "contract.pdf",
        MediaType.APPLICATION_PDF_VALUE, "pdf-content".getBytes());
    String email = "john@example.com";

    given(storageService.storeFile(any(), anyString())).willReturn("/tmp/uploads/documents/john/uuid_contract.pdf");
    given(storageService.registerDocument(eq(email), anyString(), eq("contract.pdf"), eq(com.techcorp.model.DocumentType.CONTRACT)))
        .willReturn(new com.techcorp.model.EmployeeDocument("id-1", email, "uuid_contract.pdf", "contract.pdf", com.techcorp.model.DocumentType.CONTRACT, java.time.LocalDateTime.now(), "/tmp/uploads/documents/john/uuid_contract.pdf"));

    mockMvc.perform(multipart("/api/files/documents/{email}", email).file(file).param("type", "CONTRACT"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.employeeEmail").value(email))
        .andExpect(jsonPath("$.originalFileName").value("contract.pdf"));
    }

    @Test
    public void oversizedFile_shouldReturn413() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "big.bin",
        MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[10]);

    given(storageService.storeFile(any(), anyString())).willThrow(new org.springframework.web.multipart.MaxUploadSizeExceededException(1L));

    mockMvc.perform(multipart("/api/files/documents/{email}", "a@b.com").file(file))
        .andExpect(status().isPayloadTooLarge());
    }

    @Test
    public void invalidExtension_shouldReturn400() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "script.exe",
        "application/octet-stream", "bad".getBytes());

    given(storageService.storeFile(any(), anyString())).willThrow(new com.techcorp.exception.InvalidFileException("Invalid extension"));

    mockMvc.perform(multipart("/api/files/documents/{email}", "a@b.com").file(file))
        .andExpect(status().isBadRequest());
    }

    @Test
    public void downloadDocument_shouldReturnFile() throws Exception {
        String email = "john@example.com";
        String docId = "id-1";
        com.techcorp.model.EmployeeDocument doc = new com.techcorp.model.EmployeeDocument(docId, email, "uuid_contract.pdf", "contract.pdf", com.techcorp.model.DocumentType.CONTRACT, java.time.LocalDateTime.now(), "/tmp/uploads/documents/john/uuid_contract.pdf");

        given(storageService.getDocument(email, docId)).willReturn(doc);
        given(storageService.loadAsResource(doc.getFilePath())).willReturn(new ByteArrayResource("pdf-content".getBytes()));

        mockMvc.perform(get("/api/files/documents/{email}/{documentId}", email, docId))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("contract.pdf")))
            .andExpect(content().bytes("pdf-content".getBytes()));
    }

    @Test
    public void deleteDocument_shouldReturnNoContent() throws Exception {
        String email = "john@example.com";
        String docId = "id-2";
        org.mockito.Mockito.doNothing().when(storageService).deleteDocument(email, docId);

        mockMvc.perform(delete("/api/files/documents/{email}/{documentId}", email, docId))
            .andExpect(status().isNoContent());
    }

    @Test
    public void uploadAndDownloadPhoto_flow() throws Exception {
        String email = "john@example.com";
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "imagedata".getBytes());

        given(storageService.storePhoto(any(), eq(email))).willReturn("/tmp/uploads/photos/john/photo.jpg");
        mockMvc.perform(multipart("/api/files/photos/{email}", email).file(file))
            .andExpect(status().isCreated());

    com.techcorp.model.Employee emp = new com.techcorp.model.Employee("John Doe", email, "ACME", com.techcorp.model.Position.PROGRAMISTA, 1000.0);
    emp.setPhotoFileName("photo.jpg");

    given(employeeService.findByEmail(email)).willReturn(Optional.of(emp));
    given(storageService.getReportsDir()).willReturn(java.nio.file.Paths.get("/tmp/reports"));
    given(storageService.loadAsResource(anyString())).willReturn(new ByteArrayResource("imagedata".getBytes()));

        mockMvc.perform(get("/api/files/photos/{email}", email))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/jpeg"))
            .andExpect(content().bytes("imagedata".getBytes()));
    }

    @Test
    public void exportCompanyPdf_shouldReturnPdf() throws Exception {
        String company = "ACME";
        // create a small PDF file in temp
        java.nio.file.Path tmp = java.nio.file.Files.createTempFile("test-company", ".pdf");
        try (org.apache.pdfbox.pdmodel.PDDocument d = new org.apache.pdfbox.pdmodel.PDDocument()) {
            d.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            d.save(tmp.toFile());
        }

        org.springframework.core.io.UrlResource res = new org.springframework.core.io.UrlResource(tmp.toUri());
        given(reportService.generatePdfForCompany(eq(company), anyString())).willReturn(res);

        mockMvc.perform(get("/api/files/reports/statistics/{companyName}", company))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("application/pdf")))
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("report.pdf")));
    }
}
