package com.naixue.nxdp.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JobLogReaderWebSocketClient extends WebSocketClient {

    private static final long WEBSOCKET_CONNECT_TIMEOUT_SECONDS = 10;

    private static final long WEBSOCKET_KEEP_ALIVE_TIME_MINUTES = 5;

    private CountDownLatch blockLatch = new CountDownLatch(1); // 阻塞

    public JobLogReaderWebSocketClient(URI serverUri, String data) {
        super(serverUri);
        try {
            connectBlocking(WEBSOCKET_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            send(data);
            final long wsCreateTime = System.currentTimeMillis();
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            while (System.currentTimeMillis()
                                    < wsCreateTime + WEBSOCKET_KEEP_ALIVE_TIME_MINUTES * 60000) {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    log.error(e.toString(), e);
                                    throw new RuntimeException(e.toString(), e);
                                }
                            }
                            try {
                                closeBlockingTimeout(WEBSOCKET_KEEP_ALIVE_TIME_MINUTES, TimeUnit.MINUTES);
                            } catch (InterruptedException e) {
                                log.error(e.toString(), e);
                                throw new RuntimeException(e.toString(), e);
                            }
                            log.debug("websocket client shutdown now >>> url={},data={}", serverUri, data);
                        }
                    })
                    .start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static void main(String[] args) throws URISyntaxException {
        new JobLogReaderWebSocketClient(
                new URI("ws://127.0.0.1:8081/websocket/271ee3e0-8515-4bbe-a7d1-449f4dec4c46"), "253109") {
            @Override
            public void onMessage(String message) {
                System.out.println(message + "=======================");
            }
        };
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.debug("WebSocket is open now!");
        new Thread(new TimeoutThread(WEBSOCKET_KEEP_ALIVE_TIME_MINUTES * 60, blockLatch)).start();
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.debug("WebSocket is close now![code={},reason={},boolean={}]", code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        log.error(ex.toString(), ex);
    }

    public void closeBlockingTimeout(long timeout, TimeUnit timeUnit) throws InterruptedException {
        blockLatch.await(timeout, timeUnit);
        super.closeBlocking();
    }

    @Override
    public void close() {
        blockLatch.countDown();
        super.close();
    }

    @Override
    public void close(int code) {
        blockLatch.countDown();
        super.close(code);
    }

    @Override
    public void close(int code, String message) {
        blockLatch.countDown();
        super.close(code, message);
    }

    @Override
    public void closeBlocking() throws InterruptedException {
        blockLatch.countDown();
        super.closeBlocking();
    }

    private class TimeoutThread implements Runnable {

        private long timeoutSeconds;

        private CountDownLatch latch;

        private long birthTime = System.currentTimeMillis();

        public TimeoutThread(long timeoutSeconds, CountDownLatch latch) {
            this.timeoutSeconds = timeoutSeconds;
            this.latch = latch;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("WebSocketTimeoutThread-" + Thread.currentThread().getId());
            try {
                while (!Thread.currentThread().isInterrupted()
                        && System.currentTimeMillis() - birthTime < timeoutSeconds * 1000) {
                    Thread.sleep(10000L);
                }
                latch.countDown();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
