package com.sky.websocket;

import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint("/ws/{sid}")
//TODO try to understand this
public class WebSocketServer {

    //store session object
    private static Map<String, Session> sessionMap = new HashMap<>();

    /**
     * The method called when the connection is successfully established
     *
     * @param session
     * @param sid
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("client: " + sid + " established connection!");
        sessionMap.put(sid, session);
    }

    /**
     * The method called when receiving msg from client
     *
     * @param msg
     * @param sid
     */
    @OnMessage
    public void onMessage(String msg, @PathParam("sid") String sid) {
        System.out.println("receive msg: " + msg + " from client: " + sid);
    }

    /**
     * the method called when disconnect
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("disconnect from client: " + sid);
        sessionMap.remove(sid);
    }

    /**
     * send msg to all client
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
