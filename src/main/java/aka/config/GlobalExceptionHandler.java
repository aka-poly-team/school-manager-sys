package aka.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Kích thước ảnh/tệp vượt quá dung lượng tối đa cho phép (50MB)! Vui lòng nén hoặc chọn file nhỏ hơn.");
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/teacher/attendance");
    }
}
