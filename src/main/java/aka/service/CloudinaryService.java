package aka.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Tải file ảnh lên Cloudinary và trả về URL HTTPS công khai
     * @param file File ảnh người dùng upload
     * @param folder Tên thư mục con trên Cloudinary (VD: "attendance", "avatars")
     * @return URL HTTPS công khai từ Cloudinary CDN
     */
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", "aka-system/" + folder,
                    "resource_type", "auto"
                )
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi tải ảnh lên Cloudinary: " + e.getMessage(), e);
        }
    }
}
