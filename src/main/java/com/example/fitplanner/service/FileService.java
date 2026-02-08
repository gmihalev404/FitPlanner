package com.example.fitplanner.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;

@Service
public class FileService {

    private final String uploadDir = "uploads/";

    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Returns the web-accessible path
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not save image file", e);
        }
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.contains("default")) {
            return;
        }

        try {
            // Extracts the filename from the "/uploads/filename.jpg" string
            Path path = Paths.get(imageUrl);
            String fileName = path.getFileName().toString();
            Path fileToDelete = Paths.get(uploadDir).resolve(fileName);

            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            System.err.println("Could not delete file: " + e.getMessage());
        }
    }
}
