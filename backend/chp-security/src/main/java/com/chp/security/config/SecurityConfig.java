package com.chp.security.config;

import com.chp.security.filter.JwtAuthenticationFilter;
import com.chp.security.handler.JsonAccessDeniedHandler;
import com.chp.security.handler.JsonAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers(
                                "/auth/**",
                                "/public/**",
                                "/ws/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()
                        // Bug3修复：科室查询仅允许GET匿名，写操作仍需鉴权
                        .requestMatchers(HttpMethod.GET, "/admin/dept/list", "/admin/dept").permitAll()
                        // 医护人员可访问的排班调拨接口（提交/查询自己的调班申请）
                        .requestMatchers("/admin/schedule/transfer").hasAnyRole("DOCTOR", "NURSE", "ADMIN")
                        // 医护人员可查询排班
                        .requestMatchers("/admin/schedule/**").hasAnyRole("DOCTOR", "NURSE", "ADMIN")
                        // 管理员接口 — 仅 ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 医护接口 — DOCTOR / NURSE / ADMIN
                        .requestMatchers("/medical/**").hasAnyRole("DOCTOR", "NURSE", "ADMIN")
                        // 居民接口 — RESIDENT
                        .requestMatchers("/resident/**").hasRole("RESIDENT")
                        // 其他需要认证
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
