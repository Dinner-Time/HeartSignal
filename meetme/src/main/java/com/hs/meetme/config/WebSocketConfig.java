package com.hs.meetme.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	
    @Override
    // 클라이언트가 메시지를 구독할 endpoint를 정의합니다.
    public void configureMessageBroker(MessageBrokerRegistry config) {
    	config.enableSimpleBroker("/send");
    }
    
    
    @Override
    // connection을 맺을때 CORS 허용합니다.
    public void registerStompEndpoints(StompEndpointRegistry registry) {
    	registry.addEndpoint("/ws")
    		.addInterceptors(new WSHandshakeInterceptor())
       		.setAllowedOrigins("http://ec2-3-141-206-88.us-east-2.compute.amazonaws.com:8000").withSockJS();
    }
    // 빌드 후 setAllowedOrigins : http://192.168.0.*:8000은 여러 컴퓨터에서 접근이 가능했었음.
}
