package com.vl.messenger.chat

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.converter.DefaultContentTypeResolver
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.GenericMessage
import org.springframework.messaging.support.MessageBuilder
import org.springframework.util.MimeTypeUtils.APPLICATION_JSON
import org.springframework.util.MultiValueMap
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

    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object: ChannelInterceptor { // specification allows pass optional STOMP session header
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
                val headers = (message as GenericMessage<*>).headers // StompHeaderAccessor and SimpMessageHeaderAccessor don't seem to work
                if ((headers["simpConnectMessage"] as GenericMessage<*>?)?.headers?.get("stompCommand") == StompCommand.CONNECT)
                    return GenericMessage(
                        ByteArray(0),
                        MessageHeaders((headers["simpConnectMessage"] as GenericMessage<*>).headers).let {
                            val newHeaders = HashMap(it)
                            newHeaders["session"] = (it["nativeHeaders"] as Map<*, *>)["simpSessionId"] // FIXME can't add header
                            MessageHeaders(newHeaders)
                        }.let {
                            val topLevelHeaders = HashMap(headers)
                            topLevelHeaders["simpConnectMessage"] = GenericMessage(ByteArray(0), it)
                            MessageHeaders(topLevelHeaders)
                        }
                    ).also { println(it) }
                return message
            }
        })
    }
}