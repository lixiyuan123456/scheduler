package com.naixue.nxdp.websocket;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.websocket.Session;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Deprecated
public class LogThread extends Thread {

    private String key;
    private String filepath;
    private Session session;

    public LogThread(String key, String filepath, Session session) {
        this.key = key;
        this.filepath = filepath;
        this.session = session;
        setName(key);
    }

    public void changeAttr(String newFilepath) {
        this.filepath = newFilepath;
    }

    /**
     * If this thread was constructed using a separate <code>Runnable</code> run object, then that
     * <code>Runnable</code> object's <code>run</code> method is called; otherwise, this method does
     * nothing and returns.
     *
     * <p>Subclasses of <code>Thread</code> should override this method.
     *
     * @see #start()
     * @see #stop()
     */
    @Override
    public void run() {
        InputStream fis = null;
        try {
            fis = new FileInputStream(filepath);
            StringBuilder res = null;
            filepath = null;
            byte[] buf = new byte[1024];
            int length = 0;
            int offset = 0;
            while (!Thread.interrupted()) {
                if (filepath != null) {
                    offset = 0;
                    // 发送清空日志窗口的标志
                    session.getBasicRemote().sendText("clearFlag-" + key);
                    fis.close();
                    fis = new FileInputStream(filepath);
                    filepath = null;
                }
                res = new StringBuilder();
                while ((length = fis.read(buf, offset, 1024)) != -1) {
                    res.append(new String(buf, 0, length, "utf-8"));
                }
                if (res.length() > 0) {
                    session.getBasicRemote().sendText(res.toString());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            log.error("log " + filepath + " is not exist", e);
        } catch (Exception e) {
            log.error("Create Log Thread", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e.toString(), e);
                }
            }
        }
    }
}
