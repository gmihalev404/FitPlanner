package com.example.fitplanner.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.nio.file.*;

@Service
public class FileService {

    private final Path uploadDir;

    public FileService(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir);
    }

    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        String fileName =
                System.currentTimeMillis() + "_" + file.getOriginalFilename();

        try {
            Files.createDirectories(uploadDir);

            Path filePath = uploadDir.resolve(fileName);
            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not save image file", e);
        }
    }

    public void deleteFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.contains("default")) {
            return;
        }

        try {
            String fileName = Paths.get(imageUrl).getFileName().toString();
            Files.deleteIfExists(uploadDir.resolve(fileName));
        } catch (IOException e) {
            System.err.println("Could not delete file: " + e.getMessage());
        }
    }
}
