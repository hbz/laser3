package de.laser.custom

import grails.plugin.springwebsocket.GrailsSimpAnnotationMethodMessageHandler
import grails.plugin.springwebsocket.GrailsWebSocketAnnotationMethodMessageHandler
import groovy.transform.CompileStatic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

@CompileStatic
@Configuration
@EnableWebSocketMessageBroker
class CustomWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    final static String WS_STOMP        = '/ws-socket'
    final static String WS_APP          = '/ws-app'
    final static String WS_TOPIC        = '/ws-topic'
    final static String WS_TOPIC_STATUS = '/ws-topic/status'

    @Override
    void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
        messageBrokerRegistry.setApplicationDestinationPrefixes( WS_APP )
        messageBrokerRegistry.enableSimpleBroker( WS_TOPIC )
        // messageBrokerRegistry.setUserDestinationPrefix( '/user' )
    }

    @Override
    void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        stompEndpointRegistry.addEndpoint( WS_STOMP ).withSockJS()
    }

    @Bean
    GrailsSimpAnnotationMethodMessageHandler grailsSimpAnnotationMethodMessageHandler(
            SubscribableChannel clientInboundChannel,
            MessageChannel clientOutboundChannel,
            SimpMessageSendingOperations brokerMessagingTemplate
    ) {
        def handler = new GrailsSimpAnnotationMethodMessageHandler(clientInboundChannel, clientOutboundChannel, brokerMessagingTemplate)
        handler.destinationPrefixes = [ WS_APP ]
        return handler
    }

    @Bean
    GrailsWebSocketAnnotationMethodMessageHandler grailsWebSocketAnnotationMethodMessageHandler(
            SubscribableChannel clientInboundChannel,
            MessageChannel clientOutboundChannel,
            SimpMessageSendingOperations brokerMessagingTemplate
    ) {
        def handler = new GrailsWebSocketAnnotationMethodMessageHandler(clientInboundChannel, clientOutboundChannel, brokerMessagingTemplate)
        handler.destinationPrefixes = [ WS_APP ]
        return handler
    }
}
