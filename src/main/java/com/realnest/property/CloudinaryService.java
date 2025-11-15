package com.realnest.property;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryService {

  private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
  private final Cloudinary cloudinary;
  private final boolean enabled;

  public CloudinaryService(Cloudinary cloudinary) {
    this.cloudinary = cloudinary;
    this.enabled =
        cloudinary.config != null
            && cloudinary.config.apiKey != null
            && !cloudinary.config.apiKey.isBlank()
            && !cloudinary.config.apiKey.startsWith("your-");
  }

  public Optional<String> upload(MultipartFile file) {
    if (file == null || file.isEmpty() || !enabled) {
      return Optional.empty();
    }
    try {
      Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
      Object secureUrl = uploadResult.get("secure_url");
      return Optional.ofNullable(secureUrl).map(Object::toString);
    } catch (IOException ex) {
      log.error("Failed to upload file to Cloudinary", ex);
      throw new IllegalStateException("Unable to upload image", ex);
    }
  }
}
