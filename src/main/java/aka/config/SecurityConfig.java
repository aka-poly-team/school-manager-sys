package aka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import aka.model.User;
import aka.service.SystemLogService;
import aka.service.UserService;
import aka.util.CookieUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService, SystemLogService systemLogService, UserService userService) throws Exception {
        http.cors(cors -> cors.disable());
        http.csrf(csrf -> csrf.disable());

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/auth/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico", "/error").permitAll()
                // Tất cả các trang Quản trị: Chỉ ADMIN mới được phép truy cập
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Trang Giáo viên: Chỉ TEACHER mới được phép truy cập
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(customAuthenticationSuccessHandler(systemLogService, userService))
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("aka-secret-remember-key")
                .tokenValiditySeconds(30 * 24 * 60 * 60)
                .userDetailsService(userDetailsService)
                .rememberMeParameter("remember-me")
            )
            .logout(logout -> logout
                .logoutRequestMatcher(request -> "/auth/logout".equals(request.getServletPath()))
                .logoutSuccessHandler((request, response, authentication) -> {
                    if (authentication != null) {
                        String username = authentication.getName();
                        User u = userService.findByUsername(username).orElse(null);
                        if (u != null) {
                            systemLogService.log(u, "ĐĂNG XUẤT", "Người dùng " + username + " vừa đăng xuất khỏi hệ thống.");
                        }
                    }
                    response.sendRedirect("/auth/login?logout");
                })
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            .userDetailsService(userDetailsService)
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/auth/access-denied")
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler(SystemLogService systemLogService, UserService userService) {
        return (request, response, authentication) -> {
            String username = authentication.getName();
            String rememberMe = request.getParameter("remember-me");

            User u = userService.findByUsername(username).orElse(null);
            if (u != null) {
                systemLogService.log(u, "ĐĂNG NHẬP", "Người dùng " + username + " vừa đăng nhập hệ thống thành công.");
            }

            if ("on".equals(rememberMe) || "true".equals(rememberMe)) {
                CookieUtils.setCookie(response, "remembered_username", username, 30 * 24 * 60 * 60);
            } else {
                CookieUtils.deleteCookie(response, "remembered_username");
            }

            response.sendRedirect("/");
        };
    }
}
