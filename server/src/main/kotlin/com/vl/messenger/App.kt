package com.vl.messenger

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

/*
[system properties]
server.address
server.port
base.url
jwt.aes.key

[environment]
MSG_DB_URL
 */
@SpringBootApplication
open class App {
    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            SpringApplicationBuilder(App::class.java)
                .web(WebApplicationType.SERVLET)
                .run(*args)
        }
    }
}