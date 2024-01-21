package com.vl.messenger.auth

import org.jboss.logging.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
    @Value("\${client.address:localhost}") clientAddress: String,
    @Value("\${client.port:80}") clientPort: String,
    @Autowired private val jwtFilter: JwtFilter
) {
    private val clients: List<String>

    init {
        clients = ArrayList()
        clients += "http://$clientAddress:$clientPort"
        if (clientPort == "80")
            clients += "http://$clientAddress"
    }

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
                            allowedOrigins = clients
                            allowedHeaders = listOf("*")
                            allowedMethods = listOf("POST", "GET", "PUT", "DELETE")
                            allowCredentials = true
                        }
                    )
                })
                .and()
            .authorizeHttpRequests()
                .requestMatchers("/auth/**", "/images/avatars/**")
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
