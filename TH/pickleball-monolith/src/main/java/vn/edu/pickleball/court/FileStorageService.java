package vn.edu.pickleball.court;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");
    private final Path directory;
    public FileStorageService(@Value("${app.uploads-dir}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }
    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ảnh sân.");
        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null) throw new IllegalArgumentException("Ảnh phải có định dạng JPG, PNG hoặc WebP.");
        try {
            Files.createDirectories(directory);
            String name = UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), directory.resolve(name));
            return "/uploads/" + name;
        } catch (IOException e) {
            throw new IllegalStateException("Không thể lưu ảnh sân.");
        }
    }
}
