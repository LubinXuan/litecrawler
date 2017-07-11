package me.robin.crawler.crawlers.p2peye.utils;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import io.webfolder.cdp.type.network.Cookie;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.downloader.HttpClientDownloader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Lubin.Xuan on 2017-07-10.
 * {desc}
 */
public class CookieUpdater {

    private static final Logger logger = LoggerFactory.getLogger(CookieUpdater.class);

    private SessionFactory factory;

    private Method clientMethod;
    private Field cookieStoreField;

    public CookieUpdater(String chromeBin) {
        Launcher launcher = new Launcher();
        factory = launcher.launch(chromeBin, Arrays.asList("--headless", "--disable-gpu"));
        try {
            clientMethod = HttpClientDownloader.class.getDeclaredMethod("getHttpClient", Site.class);
            clientMethod.setAccessible(true);
            cookieStoreField = Class.forName("org.apache.http.impl.client.InternalHttpClient").getDeclaredField("cookieStore");
            cookieStoreField.setAccessible(true);
        } catch (Throwable e) {
            throw new Error("HttpClient 初始化异常", e);
        }
    }

    private final AtomicBoolean update = new AtomicBoolean(false);

    public void update(HttpDownloader downloader, Site site, Request request) {
        if (update.compareAndSet(false, true)) {
            try (Session session = factory.create()) {
                session.navigate(request.getUrl());
                session.waitDocumentReady();
                List<Cookie> cookieList = session.getCommand().getPage().getCookies();
                if (null != cookieList && !cookieList.isEmpty()) {
                    try {
                        CookieStore cookieStore = (CookieStore) cookieStoreField.get(clientMethod.invoke(downloader, site));
                        for (Cookie cookie : cookieList) {
                            BasicClientCookie clientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
                            clientCookie.setDomain(cookie.getDomain());
                            clientCookie.setPath(cookie.getPath());
                            clientCookie.setDomain(".p2peye.com");
                            clientCookie.setAttribute(ClientCookie.DOMAIN_ATTR, "1");
                            cookieStore.addCookie(clientCookie);
                        }
                    } catch (Exception e) {
                        logger.warn("Cookie 信息设置异常", e);
                    }
                }
            } finally {
                update.set(false);
                synchronized (update) {
                    update.notifyAll();
                }
            }
        } else {
            synchronized (update) {
                try {
                    update.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void waitCookieUpdate() {
        if (update.get()) {
            synchronized (update) {
                try {
                    update.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
