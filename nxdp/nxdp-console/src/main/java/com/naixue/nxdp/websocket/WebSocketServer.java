package com.naixue.nxdp.websocket;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bj58.zhuanzhuan.zzarch.common.util.SystemEnvUtil;
import com.naixue.zzdp.common.util.ShellUtils;
import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.dao.JobExecuteLogRepository;
import com.naixue.nxdp.model.JobExecuteLog;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ServerEndpoint("/websocket/{webSocketKey}")
public class WebSocketServer {

    private static final String LOCAL_HOST = SystemEnvUtil.getIp();
    private static JobExecuteLogRepository jobExecuteLogRepository;
    @Getter
    private Session webSocketSession;
    @Getter
    private String webSocketKey;
    private ShellUtils.StreamGobbler streamGobbler;

    @Autowired
    public void setJobExecuteLogRepository(JobExecuteLogRepository jobExecuteLogRepository) {
        WebSocketServer.jobExecuteLogRepository = jobExecuteLogRepository;
    }

    @OnOpen
    public void onOpen(Session webSocketSession, @PathParam("webSocketKey") String webSocketKey) {
        this.webSocketSession = webSocketSession;
        this.webSocketKey = webSocketKey;
    }

    @OnClose
    public void onClose(Session session, @PathParam("webSocketKey") String webSocketKey) {
        // interruptThread(webSocketKey);
        if (this.streamGobbler != null) {
            this.streamGobbler.interrupt();
        }
    }

    @OnMessage
    public void onMessage(
            Session session, @PathParam("webSocketKey") String webSocketKey, String executeId) {
        log.info("job execute id is {}", executeId);
        if (this.streamGobbler != null && !this.streamGobbler.getName().equals(executeId)) {
            try {
                ((Session) this.streamGobbler.getHandler())
                        .getBasicRemote()
                        .sendText("clearFlag-" + webSocketKey);
            } catch (IOException e) {
                log.error(e.toString(), e);
                throw new RuntimeException(e);
            }
            this.streamGobbler.interrupt();
        }
        final String command =
                "tail -c +0 -f " + String.format(CFG.HADOOP_JOB_EXECUTE_LOG_FILE_PATH, executeId);
        JobExecuteLog jobExecuteLog = jobExecuteLogRepository.findOne(Integer.parseInt(executeId));
        final String shellCommand =
                LOCAL_HOST.equals(jobExecuteLog.getTargetServer())
                        ? command
                        : "kinit -kt /etc/krb5.keytab && ssh work@"
                        + jobExecuteLog.getTargetServer()
                        + " "
                        + command;
        streamGobbler =
                new ShellUtils.StreamGobbler(executeId, session) {
                    @Override
                    public void handle(String data) {
                        log.info("shell response:" + data + System.lineSeparator());
                        try {
                            ((Session) this.getHandler())
                                    .getBasicRemote()
                                    .sendText(data + System.lineSeparator());
                        } catch (IOException e) {
                            log.error(e.toString(), e);
                            throw new RuntimeException(e);
                        }
                    }
                };
        ShellUtils.exec(streamGobbler, "sh", "-c", shellCommand);
    /*try {
      String filepath = String.format(CFG.HADOOP_JOB_EXECUTE_LOG_FILE_PATH, executeId);
      LogThread currThread = (LogThread) getThread(webSocketKey);
      if (currThread == null) {
        currThread = new LogThread(webSocketKey, filepath, session);
        currThread.start();
      } else {
        currThread.changeAttr(filepath);
      }
    } catch (Exception e) {
      log.error("websocket", e);
    }*/
    }

    @OnError
    public void onError(
            Session session, @PathParam("webSocketKey") String webSocketKey, Throwable error) {
        log.error(webSocketKey, error);
        // interruptThread(webSocketKey);
        if (this.streamGobbler != null) {
            this.streamGobbler.interrupt();
        }
    }

    @Deprecated
    void interruptThread(String threadKey) {
        Thread currThread = getThread(threadKey);
        if (currThread != null) {
            currThread.interrupt();
        }
    }

    @Deprecated
    private Thread getThread(String threadKey) {
        Thread currThread = null;
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (threadKey.equals(thread.getName())) {
                currThread = thread;
                break;
            }
        }
        return currThread;
    }
}
