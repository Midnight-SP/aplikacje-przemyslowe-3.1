package com.techcorp.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FileStorageInitializer implements CommandLineRunner {

    @Value("${app.upload.directory:uploads/}")
    private String uploadsDir;

    @Value("${app.reports.directory:reports/}")
    private String reportsDir;

    @Override
    public void run(String... args) throws Exception {
        try {
            Files.createDirectories(Path.of(uploadsDir));
            Files.createDirectories(Path.of(reportsDir));
        } catch (IOException e) {
            // log and continue - FileStorageService will also ensure dirs exist
            System.err.println("Could not create directories: " + e.getMessage());
        }
    }
}
