package org.yungu.socketapi.websocket.socketModel;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.MimeType;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

public class StompClientDemo {
    public static void main(String[] args) {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient webSocketStompClient = new WebSocketStompClient(webSocketClient);
        //配置convert, 这里配置了对象与json互转
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
        //配置心跳
        webSocketStompClient.setDefaultHeartbeat(new long[]{10000L, 10000L});
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.afterPropertiesSet();
        webSocketStompClient.setTaskScheduler(taskScheduler);
        //连接
        webSocketStompClient.connect("ws://地址/stomp/echo", new MyHandler());

        try {
            //hang信进程, 如果不hang, 进程线程websocket连接也就都关闭了
            Thread.sleep(100000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class MyHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            stompSession.subscribe("/topic/chat", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Message.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    System.out.println("收到消息:" + payload);
                }
            });

            StompHeaders sendStompHeaders = new StompHeaders();
            stompHeaders.setDestination("/app/send");
            stompHeaders.setContentType(MimeType.valueOf("application/json;charset=utf-8"));

            //这个Message是我自己随便定义的一个结构体, 这个可以自己定义
            Message message = new Message();
            message.setType(1);
            message.setContent("hello world!");

            stompSession.send(sendStompHeaders, message);
        }
    }

}