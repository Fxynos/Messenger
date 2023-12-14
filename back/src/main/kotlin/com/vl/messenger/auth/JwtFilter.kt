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
        const val TOKEN_COOKIE_NAME = "access_token"
    }

    /**
     * Accepts bearer token from *Authorization* header or *access_token* cookie
     */
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        ((request as HttpServletRequest).getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(TOKEN_TYPE) }
            ?.substring(TOKEN_TYPE.length)
            ?.trim()
            ?: request.cookies?.firstOrNull { it.name == TOKEN_COOKIE_NAME }
                ?.value
        )?.also {
            SecurityContextHolder.getContext().authentication = jwtService.authenticate(it)
        }
        chain.doFilter(request, response)
    }
}