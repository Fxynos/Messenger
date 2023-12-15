package com.vl.messenger.chat

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.converter.DefaultContentTypeResolver
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.util.MimeTypeUtils.APPLICATION_JSON
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer


@Configuration
@EnableWebSocketMessageBroker
open class WebSocketMessageConfig: WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/user") // broker for subscriptions and broadcasting
        registry.setApplicationDestinationPrefixes("/app") // controllers routes
        //registry.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/ws") // http endpoint for websocket handshake
            .setAllowedOrigins("*")
            //.withSockJS() // some browsers don't support websocket, those use this feature
    }

    override fun configureMessageConverters(messageConverters: MutableList<MessageConverter>): Boolean {
        messageConverters.add(MappingJackson2MessageConverter().apply {
            objectMapper = ObjectMapper()
            contentTypeResolver = DefaultContentTypeResolver().apply {
                defaultMimeType = APPLICATION_JSON
            }
        })
        return false
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object : ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
                println("intercepted ${message.payload}")
                return message
            }
        })
    }
}