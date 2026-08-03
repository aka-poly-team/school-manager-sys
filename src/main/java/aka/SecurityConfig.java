package aka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import aka.service.CustomUserDetailsService;
import aka.util.CookieUtils;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.disable());
        http.csrf(csrf -> csrf.disable());

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/auth/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico", "/error").permitAll()
                // 3 Trang Quản trị thuần túy: Chỉ ADMIN mới được phép truy cập
                .requestMatchers("/admin/teachers", "/admin/teachers/**", "/admin/documents", "/admin/documents/**", "/admin/system-logs", "/admin/system-logs/**").hasRole("ADMIN")
                // Các trang Vận hành cơ sở: Cả ADMIN và MANAGER đều truy cập được
                .requestMatchers("/admin/**", "/manager/**").hasAnyRole("ADMIN", "MANAGER")
                // Trang Giáo viên: TEACHER, MANAGER và ADMIN đều truy cập được
                .requestMatchers("/teacher/**").hasAnyRole("TEACHER", "ADMIN", "MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(customAuthenticationSuccessHandler())
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("aka-secret-remember-key")
                .tokenValiditySeconds(30 * 24 * 60 * 60)
                .userDetailsService(customUserDetailsService)
                .rememberMeParameter("remember-me")
            )
            .logout(logout -> logout
                .logoutRequestMatcher(request -> "/auth/logout".equals(request.getServletPath()))
                .logoutSuccessUrl("/auth/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            .userDetailsService(customUserDetailsService)
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/auth/access-denied")
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String username = authentication.getName();
            String rememberMe = request.getParameter("remember-me");

            if ("on".equals(rememberMe) || "true".equals(rememberMe)) {
                CookieUtils.setCookie(response, "remembered_username", username, 30 * 24 * 60 * 60);
            } else {
                CookieUtils.deleteCookie(response, "remembered_username");
            }

            response.sendRedirect("/");
        };
    }
}
