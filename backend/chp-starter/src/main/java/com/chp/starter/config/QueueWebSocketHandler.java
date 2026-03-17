package com.chp.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class QueueWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(QueueWebSocketHandler.class);

    private static final Map<String, Set<WebSocketSession>> DEPT_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String dept = extractDeptCode(session);
        if (dept != null) {
            DEPT_SESSIONS.computeIfAbsent(dept, k -> new CopyOnWriteArraySet<>()).add(session);
            log.info("[WS] 公屏连接: dept={}, id={}", dept, session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String dept = extractDeptCode(session);
        if (dept != null) {
            Set<WebSocketSession> sessions = DEPT_SESSIONS.get(dept);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) DEPT_SESSIONS.remove(dept);
            }
        }
    }

    public static void notify(String deptCode, String jsonMessage) {
        Set<WebSocketSession> sessions = DEPT_SESSIONS.get(deptCode);
        if (sessions == null || sessions.isEmpty()) return;
        TextMessage msg = new TextMessage(jsonMessage);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try { s.sendMessage(msg); } catch (IOException e) {
                    log.warn("[WS] 推送失败: {}", e.getMessage());
                }
            }
        }
    }

    private String extractDeptCode(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String path = uri.getPath();
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 && lastSlash < path.length() - 1 ? path.substring(lastSlash + 1) : null;
    }
}
