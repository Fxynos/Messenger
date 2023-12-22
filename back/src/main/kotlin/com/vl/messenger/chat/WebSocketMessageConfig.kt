package com.vl.messenger.chat

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.converter.DefaultContentTypeResolver
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.access.AccessDeniedException
import org.springframework.util.MimeTypeUtils.APPLICATION_JSON
import org.springframework.util.MultiValueMap
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer


@Configuration
@EnableWebSocketMessageBroker
open class WebSocketMessageConfig: WebSocketMessageBrokerConfigurer {
    companion object {
        const val STOMP_CONNECT_MESSAGE = "simpConnectMessage"
    }

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
        registration.interceptors(object: ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
                val accessor = StompHeaderAccessor.wrap(message)
                if (accessor.command == StompCommand.SUBSCRIBE && !accessor.destination!!.endsWith(accessor.user!!.name))
                    throw AccessDeniedException("Forbidden")
                return message
            }
        })
    }

    /*override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object: ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
                val accessor = StompHeaderAccessor.wrap(message)
                val connectAccessor = (accessor.messageHeaders[STOMP_CONNECT_MESSAGE] as Message<*>?)
                    ?.let { StompHeaderAccessor.wrap(it) }
                println("old message: $message") // TODO remove
                if (connectAccessor?.command == StompCommand.CONNECT) {
                    val customConnectAccessor = StompHeaderAccessor.create(StompCommand.CONNECT)
                    customConnectAccessor.sessionId = connectAccessor.sessionId
                    @Suppress("UNCHECKED_CAST")
                    customConnectAccessor.addNativeHeaders(
                        connectAccessor.getHeader(StompHeaderAccessor.NATIVE_HEADERS) as MultiValueMap<String, String>
                    )
                    customConnectAccessor.addNativeHeader("session", connectAccessor.sessionId) // affects only OOP model, not actual STOMP frame
                    val customAccessor = StompHeaderAccessor.create(
                        accessor.getHeader(StompHeaderAccessor.MESSAGE_TYPE_HEADER) as SimpMessageType
                    )
                    customAccessor.sessionId = accessor.sessionId
                    customAccessor.setHeader(
                        STOMP_CONNECT_MESSAGE,
                        MessageBuilder.createMessage(ByteArray(0), customConnectAccessor.messageHeaders)
                    )
                    return MessageBuilder.createMessage(ByteArray(0), customAccessor.messageHeaders).also {
                        println("new message: $it") // TODO remove
                    }
                }
                return message
            }
        })
    }*/
}