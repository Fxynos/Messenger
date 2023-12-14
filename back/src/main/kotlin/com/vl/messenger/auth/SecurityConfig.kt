package com.vl.messenger.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
@Configuration
open class SecurityConfig(
    @Autowired private val jwtFilter: JwtFilter
) {
    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic().disable()
            .csrf().disable()
            .cors()
                .configurationSource(UrlBasedCorsConfigurationSource().apply {
                    registerCorsConfiguration(
                        "/**",
                        CorsConfiguration().apply {
                            allowedOrigins = listOf("*")
                            allowedHeaders = listOf("*")
                            allowedMethods = listOf("POST", "GET", "PUT", "DELETE")
                        }
                    )
                })
                .and()
            .authorizeHttpRequests()
                .requestMatchers("/auth/**")
                .permitAll()
                .and()
            .authorizeHttpRequests()
                .anyRequest()
                .authenticated()
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .addFilterAfter(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}