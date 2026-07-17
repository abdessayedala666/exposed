package com.game.exposed.Config;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
        ServerHttpRequest request,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) {
        Object sessionIdAttr = attributes.get("HTTP.SESSION.ID");
        Object nameAttr = attributes.get("name");

        String sessionId = sessionIdAttr != null ? sessionIdAttr.toString() : UUID.randomUUID().toString();
        String name = nameAttr != null ? nameAttr.toString() : "anonymous";

        String username = sessionId + ":" + name;

        return () -> username;
    }
}