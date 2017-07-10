package me.robin.crawler.p2peye.utils;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.selector.PlainText;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    @Override
    public Page download(Request request, Task task) {


        Session session = sessions.poll();

        try {
            if (null == session) {
                session = factory.create();
            }

            session.navigate(request.getUrl());

            session.waitDocumentReady();

            String content = (String) session.getProperty("*","outerHTML");

            session.getCommand().getPage();

            Page page = new Page();
            page.setRawText(content);
            page.setUrl(new PlainText(request.getUrl()));
            page.setRequest(request);
            page.setStatusCode(200);
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
