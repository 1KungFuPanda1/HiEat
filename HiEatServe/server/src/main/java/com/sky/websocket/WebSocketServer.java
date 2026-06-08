package com.sky.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * WebSocket服务
 */
@Slf4j
@Component
@ServerEndpoint("/ws/{sid}")//前端可以通过 ws://ip: 端口 /ws/ 任意 id 连接到后端
// 前端连接地址：ws://localhost:8080/ws/
public class  WebSocketServer {

//    session-id-001  -> Session对象
//    session-id-002  -> Session对象
//    session-id-003  -> Session对象
//    session-id-004  -> Session对象
    // 存放会话对象
    private static Map<String, Session> sessionMap = new HashMap();

//
//    1001 -> [session-id-001, session-id-002]
//    1002 -> [session-id-003, session-id-004]
    // 存放shopId与多个Session的映射关系
    private static Map<Long, Set<String>> shopSessionMap = new HashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        log.info("客户端sid：{}与服务器建立连接", sid);
        System.out.println("客户端：" + sid + "建立连接");
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到来自客户端：{}的信息:{}", sid, message);

        try {
            JSONObject jsonObject = JSON.parseObject(message);
            if ("init".equals(jsonObject.getString("type"))) {
                Long shopId = jsonObject.getLong("shopId");
                // 将shopId与sid关联
                shopSessionMap.computeIfAbsent(shopId, k -> new HashSet<>()).add(sid);
                log.info("客户端{}已绑定到商家{}", sid, shopId);
            }
        } catch (Exception e) {
            log.error("处理客户端消息失败", e);
        }
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        sessionMap.remove(sid);

        // 从shopSessionMap中移除该sid
        shopSessionMap.values().forEach(sessions -> sessions.remove(sid));
        // 清理空的shopId映射
        shopSessionMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }


    /**
     * 向指定商家发送消息
     * 
     * @param message 消息内容
     * @param shopId  商家ID
     */
    public void sendToShop(String message, Long shopId) {
        Set<String> sessionIds = shopSessionMap.get(shopId);
        if (sessionIds != null && !sessionIds.isEmpty()) {
            for (String sid : sessionIds) {
                Session session = sessionMap.get(sid);
                if (session != null) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (Exception e) {
                        log.error("向商家{}的客户端{}发送消息失败", shopId, sid, e);
                    }
                }
            }
        }
    }

}
