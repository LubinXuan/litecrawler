package me.robin.crawler.crawlers.p2peye.utils;

import me.robin.crawler.common.OkHttpDownloader;
import okhttp3.Response;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;

import java.io.IOException;

/**
 * Created by Lubin.Xuan on 2017-07-10.
 * {desc}
 */
public class HttpDownloader extends OkHttpDownloader {

    private final CookieUpdater cookieUpdater;

    public HttpDownloader(CookieUpdater cookieUpdater) {
        this.cookieUpdater = cookieUpdater;
    }

    @Override
    protected void handleResponse(Request request, String charset, Response httpResponse, Task task,Page page) throws IOException {
        super.handleResponse(request, charset, httpResponse, task,page);
        if (page.getStatusCode() == 521) {
            page.setDownloadSuccess(false);
            this.cookieUpdater.update(this, task.getSite(), request);
        }
    }
}
