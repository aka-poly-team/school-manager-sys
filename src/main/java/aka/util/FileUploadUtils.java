package aka.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility library for handling file uploads.
 */
public class FileUploadUtils {

    public static String save(MultipartFile file, String subDirectory, String filePrefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String relativeUploadDir = "uploads/" + subDirectory + "/";
            
            File srcDir = new File("src/main/resources/static/" + relativeUploadDir);
            if (!srcDir.exists()) srcDir.mkdirs();

            File targetDir = new File("target/classes/static/" + relativeUploadDir);
            if (!targetDir.exists()) targetDir.mkdirs();

            String fileName = filePrefix + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            
            File srcFile = new File(srcDir.getAbsolutePath() + File.separator + fileName);
            file.transferTo(srcFile);

            try {
                File targetFile = new File(targetDir.getAbsolutePath() + File.separator + fileName);
                Files.copy(srcFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ignored) {}

            return "/" + relativeUploadDir + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu file: " + e.getMessage(), e);
        }
    }
}
