package com.vl.messenger.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

@Component
class JwtFilter(@Autowired private val jwtService: JwtService): GenericFilterBean() {
    companion object {
        const val TOKEN_TYPE = "Bearer"
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val authorization = (request as HttpServletRequest).getHeader(HttpHeaders.AUTHORIZATION)
        if (authorization?.startsWith(TOKEN_TYPE) == true) {
            val token = authorization.substring(TOKEN_TYPE.length).trim()
            SecurityContextHolder.getContext().authentication = jwtService.authenticate(token)
        }
        chain.doFilter(request, response)
    }
}