package com.naixue.nxdp.websocket;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ServerEndpoint("/video-server")
public class VideoWebsocketServer {

    public static void main(String[] args) throws Exception {
        File file = ResourceUtils.getFile("file:///d:/test.mp4");
        System.out.println(file.length());
    }

    @OnMessage
    public void OnMessage(final Session session, final String text) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try (RandomAccessFile file =
                     new RandomAccessFile(
                             ResourceUtils.getFile("classpath:test.webm"),
                             // ResourceUtils.getFile("file:///d:/test.mp4"),
                             "rw");) {
            FileChannel fileChannel = file.getChannel();
            while (fileChannel.read(buffer) > 0) {
                buffer.flip();
                session.getBasicRemote().sendBinary(buffer);
                buffer.clear();
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    @OnOpen
    public void onOpen(final Session session) {
        log.debug("*********************open******************");
    }

    @OnClose
    public void OnClose(final Session session) {
        log.debug("*********************open******************");
    }

    @OnError
    public void onError(final Session session, final Throwable error) {
        log.debug("*********************error******************");
    }
}
