package me.robin.crawler.p2peye.utils;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.event.Events;
import io.webfolder.cdp.event.network.ResponseReceived;
import io.webfolder.cdp.listener.EventListener;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import io.webfolder.cdp.type.network.GetResponseBodyResult;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.selector.PlainText;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Lubin.Xuan on 2017-07-10.
 * {desc}
 */
public class ChromeDownloader extends AbstractDownloader {
    private final SessionFactory factory;

    private int threadNum;

    private BlockingQueue<Session> sessions = new LinkedBlockingQueue<>();

    public ChromeDownloader(String chromeBin) {
        Launcher launcher = new Launcher();
        factory = launcher.launch(chromeBin, Arrays.asList("--headless", "--disable-gpu"));
    }

    private synchronized Session createSession() {
        Session session = sessions.poll();

        if (null == session) {
            session = factory.create();
            session.getCommand().getNetwork().enable();
        }

        return session;
    }

    @Override
    public Page download(Request request, Task task) {


        Session session = createSession();

        try {

            Page page = new Page();

            session.addEventListener(new EventListener<Object>() {
                @Override
                public void onEvent(Events event, Object value) {
                    if (Events.NetworkResponseReceived.equals(event) && value instanceof ResponseReceived) {
                        ResponseReceived received = (ResponseReceived) value;
                        if (received.getResponse().getUrl().equals(request.getUrl())) {
                            GetResponseBodyResult rb = session.getCommand().getNetwork().getResponseBody(received.getRequestId());
                            page.setRawText(rb.getBody());
                            page.setStatusCode(received.getResponse().getStatus().intValue());
                            synchronized (page) {
                                page.notify();
                            }
                            session.removeEventEventListener(this);
                        }
                    }
                }
            });
            session.navigate(request.getUrl());
            page.setUrl(new PlainText(request.getUrl()));
            page.setRequest(request);
            return page;
        } finally {
            if (null != session) {
                sessions.offer(session);
            }
        }
    }

    @Override
    public void setThread(int threadNum) {
        this.threadNum = threadNum;
    }
}
