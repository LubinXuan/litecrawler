package me.robin.crawler.crawlers.p2peye.utils;

import org.apache.http.HttpResponse;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.HttpClientDownloader;

import java.io.IOException;

/**
 * Created by Lubin.Xuan on 2017-07-10.
 * {desc}
 */
public class HttpDownloader extends HttpClientDownloader {

    private final CookieUpdater cookieUpdater;

    public HttpDownloader(CookieUpdater cookieUpdater) {
        this.cookieUpdater = cookieUpdater;
    }

    @Override
    protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException {
        Page page = super.handleResponse(request, charset, httpResponse, task);
        if (page.getStatusCode() == 521) {
            page.setDownloadSuccess(false);
            this.cookieUpdater.update(this, task.getSite(), request);
        }
        return page;
    }
}
