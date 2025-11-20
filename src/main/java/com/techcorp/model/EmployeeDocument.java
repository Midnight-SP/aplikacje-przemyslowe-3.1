package com.techcorp.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class EmployeeDocument {
    private final String id;
    private final String employeeEmail;
    private final String fileName;
    private final String originalFileName;
    private final DocumentType fileType;
    private final LocalDateTime uploadDate;
    private final String filePath;

    public EmployeeDocument(String id,
                            String employeeEmail,
                            String fileName,
                            String originalFileName,
                            DocumentType fileType,
                            LocalDateTime uploadDate,
                            String filePath) {
        this.id = Objects.requireNonNull(id);
        this.employeeEmail = Objects.requireNonNull(employeeEmail);
        this.fileName = Objects.requireNonNull(fileName);
        this.originalFileName = Objects.requireNonNull(originalFileName);
        this.fileType = Objects.requireNonNull(fileType);
        this.uploadDate = Objects.requireNonNull(uploadDate);
        this.filePath = Objects.requireNonNull(filePath);
    }

    public String getId() {
        return id;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public String getFileName() {
        return fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public DocumentType getFileType() {
        return fileType;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "EmployeeDocument{" +
                "id='" + id + '\'' +
                ", employeeEmail='" + employeeEmail + '\'' +
                ", fileName='" + fileName + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", fileType=" + fileType +
                ", uploadDate=" + uploadDate +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
