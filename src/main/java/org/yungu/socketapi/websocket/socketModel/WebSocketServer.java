package org.yungu.socketapi.websocket.socketModel;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
/**
 * @author:牟振东
 * @method
    * @param
 * @date: Created in ${TIME} ${DATE}
 * @description
 */
@ServerEndpoint(value = "/websocket")
@Component
public class WebSocketServer extends TextWebSocketHandler {
     /**这个会出现性能问题，最好用Map来存储，key用userid*/
    private static final ArrayList<WebSocketSession> users ;
    static {
        users = new ArrayList<>();
    }
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。*/
    private static int onlineCount = 0;
    /**concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。*/
   private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;

    /**
     * 通过前端传过来的session 判断 当前有多少人连接上来
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        //加入set中
        webSocketSet.add(this);
        addOnlineCount();           //在线数加1
        logger.info("有新连接加入！当前在线人数为" + getOnlineCount());
        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            logger.error("websocket IO异常");
        }
    }


    /**
     * 连接成功时候，会触发UI上onopen方法  接受离线消息
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("connect to the websocket success......");
        users.add(session);
        // 这块会实现自己业务，比如，当用户登录后，会把离线消息推送给用户
         TextMessage returnMessage = new TextMessage("你将收到的离线");
         session.sendMessage(returnMessage);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        logger.debug("websocket connection closed......");
        users.remove(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.debug("websocket connection closed......");
        users.remove(session);
    }
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        //从set中删除
        webSocketSet.remove(this);
        //在线数减1
        subOnlineCount();
        logger.info("有一连接 关闭！当前在线人 数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * 前端传过来的一个需要走这个方法
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("来自客户端的消息:" + message);

        //群发消息
        for (WebSocketServer item : webSocketSet) {
            try {
                if(message!=null&&!message.equals("")){
                    item.sendMessage(message);
                }else{
                    item.sendMessage("您输入了null 值，请确认");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("发生错误");
        error.printStackTrace();
    }


  public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    /**
     * 群发自定义消息
     * */
    public static void sendInfo(String message)  {
        System.out.println(message);
        for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}