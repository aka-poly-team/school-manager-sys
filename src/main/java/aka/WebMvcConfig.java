package aka;

import java.io.File;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configure Spring MVC to serve dynamic runtime uploads from the filesystem.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File uploadsDir = new File("src/main/resources/static/uploads/");
        String uploadsPath = uploadsDir.getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath + File.separator, "classpath:/static/uploads/");
    }
}
