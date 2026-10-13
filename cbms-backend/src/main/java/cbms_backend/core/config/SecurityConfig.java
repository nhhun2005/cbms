package cbms_backend.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Tạo tài khoản không cần xác thực
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        // Các thao tác quản lý user yêu cầu xác thực (sẽ gắn role khi tích hợp JWT)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/**").authenticated()
                        // Mọi request khác đều phải xác thực
                        .anyRequest().authenticated());

        return httpSecurity.build();
    }
}
