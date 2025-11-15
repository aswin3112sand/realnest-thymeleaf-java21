package com.realnest.property;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores uploaded images either remotely (Cloudinary) or locally under {@code uploads/}.
 */
@Service
public class ImageStorageService {

  private final CloudinaryService cloudinaryService;

  public ImageStorageService(CloudinaryService cloudinaryService) {
    this.cloudinaryService = cloudinaryService;
  }

  public Optional<String> store(MultipartFile file, String folder) {
    if (file == null || file.isEmpty()) {
      return Optional.empty();
    }
    Optional<String> remoteUrl = cloudinaryService.upload(file);
    if (remoteUrl.isPresent()) {
      return remoteUrl;
    }
    try {
      Path destinationDir = Paths.get("uploads").resolve(folder);
      Files.createDirectories(destinationDir);
      String filename = buildFilename(file.getOriginalFilename());
      Path destination = destinationDir.resolve(filename);
      Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
      return Optional.of("/uploads/" + folder + "/" + filename);
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to store image", ex);
    }
  }

  private String buildFilename(String originalFilename) {
    String safeName =
        originalFilename == null || originalFilename.isBlank()
            ? "image"
            : sanitize(originalFilename);
    String base;
    String extension = "";
    int dotIndex = safeName.lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < safeName.length() - 1) {
      base = safeName.substring(0, dotIndex);
      extension = safeName.substring(dotIndex).toLowerCase(Locale.ROOT);
    } else {
      base = safeName;
    }
    String normalized = base.isBlank() ? "image" : base;
    return System.currentTimeMillis() + "-" + normalized + extension;
  }

  private String sanitize(String value) {
    return value.replaceAll("[^A-Za-z0-9\\.\\-]", "_");
  }
}

