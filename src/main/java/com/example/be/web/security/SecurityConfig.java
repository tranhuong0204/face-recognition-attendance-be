package com.example.be.web.security;

import com.example.be.web.security.jwt.JwtAuthenticationEntryPoint;
import com.example.be.web.security.jwt.JwtAuthenticationFilter;
import com.example.be.web.service.impl.CustomUserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true) //bị quên cái này
//@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    CustomUserDetailsServiceImpl customUserDetailsService;

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


//    public SecurityConfig(UserDetailsService userDetailsService) {
//        this.userDetailsService = userDetailsService;
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) throws Exception {
        http
                // 1. Cấu hình CORS (Phải đặt trước authorizeHttpRequests)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Tắt CSRF vì chúng ta dùng JWT (Stateless)
                .csrf(csrf -> csrf.disable())


                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // 4. Phân quyền Request
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",           // Cho phép đăng nhập, đăng ký
                                "/api/public/**",     // Các API công khai (nếu có)
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated() // Tất cả các request khác phải có Token
                )

                // 5. Cấu hình Stateless (Không lưu Session trên Server)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 6. Tắt các chế độ đăng nhập mặc định của trình duyệt
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 7. Thêm Provider và Filter xử lý JWT
                .authenticationProvider(daoAuthProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 8. Bean cấu hình CORS để Flutter gọi được API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phép các nguồn cụ thể hoặc "*" (tất cả) để test đồ án cho nhanh
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        DaoAuthenticationProvider daoAuthProvider = new DaoAuthenticationProvider();
        daoAuthProvider.setUserDetailsService(customUserDetailsService);
        daoAuthProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthProvider;
    }



}


