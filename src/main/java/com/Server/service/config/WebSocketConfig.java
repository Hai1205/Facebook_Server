package com.Server.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${client.url}")
    private String clientUrl;

    @Bean(name = "webSocketHeartbeatScheduler")
    public TaskScheduler webSocketHeartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("websocket-heartbeat-thread-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue", "/user")
                .setHeartbeatValue(new long[] { 10000, 10000 }) // Cấu hình heartbeat để giữ kết nối
                .setTaskScheduler(this.webSocketHeartbeatTaskScheduler());
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(clientUrl)
                .withSockJS()
                .setWebSocketEnabled(true)
                .setSessionCookieNeeded(false);

        // Thêm endpoint không dùng SockJS cho các client khác
        registry.addEndpoint("/ws")
                .setAllowedOrigins(clientUrl);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024); // 64KB
        registration.setSendBufferSizeLimit(512 * 1024); // 512KB
        registration.setSendTimeLimit(20000); // 20 seconds
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            // Có thể thêm các interceptor xử lý header hoặc lưu thông tin người dùng vào
            // session
        });

        // Cấu hình số luồng xử lý tin nhắn đến
        registration.taskExecutor().corePoolSize(4);
        registration.taskExecutor().maxPoolSize(10);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // Cấu hình số luồng xử lý tin nhắn đi
        registration.taskExecutor().corePoolSize(4);
        registration.taskExecutor().maxPoolSize(10);
    }
}