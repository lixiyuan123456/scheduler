package com.bountyhunter.tomato.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class DefaultWebSocketClient extends WebSocketClient {

    public DefaultWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public static void main(String[] args) throws Exception {
        WebSocketClient client =
                new DefaultWebSocketClient(
                        new URI(
                                "ws://127.0.0.1:8081/socket.io/1/websocket/271ee3e0-8515-4bbe-a7d1-449f4dec4c46"));
        client.connect();
        while (!client.isOpen()) {
        }
        client.send("253109");
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onMessage(String message) {
        System.out.println("~~" + message + "~~");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        System.out.println("信息");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("关闭");
    }

    @Override
    public void onError(Exception ex) {
    }
}
