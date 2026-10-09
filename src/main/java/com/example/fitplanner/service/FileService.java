
package com.example.fitplanner.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class FileService {

    private final Cloudinary cloudinary;
    private final Path uploadDir;
    private final String cloudName;

    public FileService(
            Cloudinary cloudinary,
            @Value("${app.upload-dir:uploads}") String uploadDir,
            @Value("${CLOUDINARY_CLOUD_NAME}") String cloudName
    ) {
        this.cloudinary = cloudinary;
        this.uploadDir = Paths.get(uploadDir);
        this.cloudName = cloudName;
    }

    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String publicId = "fitplanner/" + UUID.randomUUID();

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "auto"
                    )
            );

            return (String) result.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Could not upload image to Cloudinary", e);
        }
    }

    public void deleteFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()
                || imageUrl.contains("default")) {
            return;
        }

        try {
            if (imageUrl.startsWith("/uploads/")) {
                String fileName = Paths.get(imageUrl).getFileName().toString();
                Files.deleteIfExists(uploadDir.resolve(fileName));
                return;
            }

            String imagePrefix =
                    "https://res.cloudinary.com/" + cloudName + "/image/upload/";

            String videoPrefix =
                    "https://res.cloudinary.com/" + cloudName + "/video/upload/";

            String resourceType;

            if (imageUrl.startsWith(imagePrefix)) {
                resourceType = "image";
            } else if (imageUrl.startsWith(videoPrefix)) {
                resourceType = "video";
            } else {
                return;
            }

            String path = imageUrl.substring(
                    resourceType.equals("image")
                            ? imagePrefix.length()
                            : videoPrefix.length()
            );

            // Remove Cloudinary transformation and version segments.
            String[] segments = path.split("/");
            int start = 0;

            while (start < segments.length &&
                    (segments[start].matches("v\\d+")
                            || segments[start].contains(","))) {
                start++;
            }

            String publicId = String.join(
                    "/",
                    java.util.Arrays.copyOfRange(
                            segments, start, segments.length
                    )
            );

            publicId = publicId.replaceFirst("\\.[^.]+$", "");

            if (!publicId.startsWith("fitplanner/")) {
                return;
            }

            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );

        } catch (Exception e) {
            System.err.println("Could not delete image: " + e.getMessage());
        }
    }
}