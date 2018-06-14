package org.yungu.socketapi.websocket.socketModel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.util.List;

@EnableWebSocketMessageBroker
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        System.out.println("服务器启动成功");
        //配置前缀, 有这些前缀的会路由到broker
        config.enableSimpleBroker("/topic", "/queue")
                //配置stomp协议里, server返回的心跳
                .setHeartbeatValue(new long[]{10000L, 10000L})
                //配置发送心跳的scheduler
                .setTaskScheduler(new DefaultManagedTaskScheduler());
        //配置前缀, 有这些前缀的会被到有@SubscribeMapping与@MessageMapping的业务方法拦截
        config.setApplicationDestinationPrefixes("/app", "/topic", "/queue");

        //这里设置的simple broker是指可以订阅的地址，也就是服务器可以发送的地址
        /**
         * userChat 用于用户聊天
         */
//        config.enableSimpleBroker("/userChat");
//        config.setApplicationDestinationPrefixes("/app");

    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //添加这个Endpoint，这样在网页中就可以通过websocket连接上服务了
        registry.addEndpoint("/coordination").withSockJS();
    }


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {

    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {

    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        return false;
    }
    /*来配置request channel与response channel线程池. 如下:*/
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.setInterceptors(new StompChannelInterceptor());
///**  其中corePoolSize为核心线程数, maxPoolSize最大线程数, queueCapacity队列容积.*/
//        registration.taskExecutor()
//                .corePoolSize(32)
//                .maxPoolSize(200)
//                .queueCapacity(10000);
    }
    /*来配置request channel与response channel线程池. 如下:*/
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
//        registration.setInterceptors(new StompChannelInterceptor());
//      /**  其中corePoolSize为核心线程数, maxPoolSize最大线程数, queueCapacity队列容积.*/
//                registration.taskExecutor()
//                .corePoolSize(100)
//                .maxPoolSize(400)
//                .queueCapacity(20000);
    }

    /**这里需要注意一点, queueCapacity的默认配置是无限大, 如果是无限大, 那么线程数则永远是核心线程数.
            只能当队列容积不够用时, 实际线程数才会大于核心线程数.

            由于server到client的网络状况以及client的处理能力很难预测, 所以合理配置response channel线程数相对比较困难.
    为此spring websocket提供了两个额外的配置来进行优化.
    继承AbstractWebSocketMessageBrokerConfigurer重写configureWebSocketTransport方法, 如下:*/
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
                .setSendTimeLimit(15 * 1000)
                .setSendBufferSizeLimit(512 * 1024);

    }

}