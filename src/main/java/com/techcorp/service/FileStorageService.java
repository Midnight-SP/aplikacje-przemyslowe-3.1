package com.techcorp.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.techcorp.exception.FileMissingException;
import com.techcorp.exception.FileStorageException;
import com.techcorp.exception.InvalidFileException;
import com.techcorp.model.DocumentType;
import com.techcorp.model.EmployeeDocument;

@Service
public class FileStorageService {

    private final Path uploadsDir;
    private final Path reportsDir;

    // in-memory metadata: email -> list of documents
    private final Map<String, List<EmployeeDocument>> documents = new ConcurrentHashMap<>();

    public FileStorageService(@Value("${app.upload.directory:uploads/}") String uploadsDir,
                              @Value("${app.reports.directory:reports/}") String reportsDir) {
        this.uploadsDir = Paths.get(uploadsDir).toAbsolutePath().normalize();
        this.reportsDir = Paths.get(reportsDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadsDir);
            Files.createDirectories(this.reportsDir);
        } catch (IOException e) {
            throw new FileStorageException("Could not create storage directories", e);
        }
    }

    public String storeFile(MultipartFile file, String subPath) {
        validateFile(file, subPath);

        try {
            Path targetFolder = uploadsDir.resolve(subPath).normalize();
            Files.createDirectories(targetFolder);
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path target = targetFolder.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file", e);
        }
    }

    public Resource loadAsResource(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                throw new FileMissingException("File not found: " + filePath);
            }
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileMissingException("File not readable: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Failed to load file as resource", e);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file", e);
        }
    }

    public void deleteDocument(String email, String documentId) {
        List<EmployeeDocument> list = documents.getOrDefault(email.toLowerCase(), Collections.emptyList());
        EmployeeDocument found = list.stream().filter(d -> d.getId().equals(documentId)).findFirst().orElse(null);
        if (found == null) {
            throw new FileMissingException("Document not found: " + documentId);
        }
        deleteFile(found.getFilePath());
        list.remove(found);
    }

    public String storePhoto(MultipartFile file, String email) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Photo is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/png"))) {
            throw new InvalidFileException("Only JPG and PNG photos are allowed");
        }
        if (file.getSize() > 2 * 1024 * 1024) { // 2MB
            throw new InvalidFileException("Photo exceeds max size 2MB");
        }
        String subPath = "photos/" + email.toLowerCase();
        return storeFile(file, subPath);
    }

    public EmployeeDocument registerDocument(String email, String storedPath, String originalFilename, DocumentType type) {
        String id = UUID.randomUUID().toString();
        EmployeeDocument doc = new EmployeeDocument(id, email, Paths.get(storedPath).getFileName().toString(), originalFilename, type, LocalDateTime.now(), storedPath);
        documents.computeIfAbsent(email.toLowerCase(), k -> Collections.synchronizedList(new ArrayList<>())).add(doc);
        return doc;
    }

    public List<EmployeeDocument> listDocuments(String email) {
        return documents.getOrDefault(email.toLowerCase(), Collections.emptyList()).stream().collect(Collectors.toList());
    }

    public EmployeeDocument getDocument(String email, String documentId) {
        return listDocuments(email).stream()
                .filter(d -> d.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new FileMissingException("Document not found: " + documentId));
    }

    private void validateFile(MultipartFile file, String subPath) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        long size = file.getSize();
        // default max 10MB for non-photo files
        long max = 10L * 1024L * 1024L;
        if (size > max) {
            throw new InvalidFileException("File exceeds max allowed size of 10MB");
        }

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String lower = original.toLowerCase();

        if (subPath != null && subPath.startsWith("imports")) {
            // allow csv and xml
            if (!(lower.endsWith(".csv") || lower.endsWith(".xml"))) {
                throw new InvalidFileException("Only CSV or XML files are allowed for import");
            }
            return;
        }

        if (subPath != null && subPath.startsWith("documents")) {
            // allow common document types
            if (!(lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".txt"))) {
                throw new InvalidFileException("Document extension not allowed");
            }
            return;
        }

        // generic check passed
    }

    public Path getReportsDir() {
        return reportsDir;
    }
}
