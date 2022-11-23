package de.laser.custom

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

@Configuration
@EnableWebSocketMessageBroker
class CustomWebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    public static final String WS_STOMP        = '/ws-stomp'
    public static final String WS_APP          = '/ws-app'
    public static final String WS_TOPIC        = '/ws-topic'
    public static final String WS_TOPIC_STATUS = '/ws-topic/status'

    @Override
    void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        //stompEndpointRegistry.addEndpoint( WS_STOMP )
        stompEndpointRegistry.addEndpoint( WS_STOMP ).withSockJS()
    }

    @Override
    void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
        messageBrokerRegistry.enableSimpleBroker( WS_TOPIC )
        //messageBrokerRegistry.enableStompBrokerRelay( WS_TOPIC )
        messageBrokerRegistry.setApplicationDestinationPrefixes( WS_APP )
        // messageBrokerRegistry.setUserDestinationPrefix( '/user' )
    }
}
