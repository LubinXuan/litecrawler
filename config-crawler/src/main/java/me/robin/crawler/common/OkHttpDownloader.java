package me.robin.crawler.common;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSink;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.CharsetUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Lubin.Xuan on 2017-09-21.
 * {desc}
 */
@Slf4j
public class OkHttpDownloader extends AbstractDownloader {

    private OkHttpClient client;

    private CookieStore cookieStore;

    public OkHttpDownloader() {
        this.client = build(null);
        this.cookieStore = new CookieManager().getCookieStore();
    }


    @Override
    public Page download(Request request, Task task) {
        if (task == null || task.getSite() == null) {
            throw new NullPointerException("task or site can not be null");
        }
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(request.getUrl());
        URI uri = URI.create(request.getUrl());
        List<HttpCookie> cookieList = this.cookieStore.get(uri);
        StringBuilder cookieBuilder = new StringBuilder();
        IterableUtils.forEach(cookieList, cookie -> {
            if (cookieBuilder.length() > 0) {
                cookieBuilder.append(";");
            }
            cookieBuilder.append(cookie.getName()).append("=").append(cookie.getValue());
        });

        IterableUtils.forEach(task.getSite().getCookies().entrySet(), input -> {
            if (cookieBuilder.length() > 0) {
                cookieBuilder.append(";");
            }
            cookieBuilder.append(input.getKey()).append("=").append(input.getValue());
        });

        if (cookieBuilder.length() > 0) {
            builder.header("Cookie", cookieBuilder.toString());
        }

        if (StringUtils.isNotBlank(task.getSite().getUserAgent())) {
            builder.header("User-Agent", task.getSite().getUserAgent());
        }

        if (StringUtils.equalsIgnoreCase("post", request.getMethod())) {
            if (null == request.getRequestBody()) {
                builder.post(new RequestBody() {
                    @Nullable
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {

                    }
                });
            } else {
                HttpRequestBody body = request.getRequestBody();
                RequestBody requestBody = RequestBody.create(MediaType.parse(body.getContentType() + ";charset=" + body.getEncoding() + ";"), body.getBody());
                builder.post(requestBody);
            }
        }

        Response response = null;

        Page page = Page.fail();

        try {
            response = client.newCall(builder.build()).execute();
            handleResponse(request, task.getSite().getCharset(), response, task, page);
            onSuccess(request);
            log.debug("downloading page success {} {}", response.code(), request.getUrl());
            return page;
        } catch (IOException e) {
            log.warn("download page {} error", request.getUrl(), e);
            onError(request);
            return page;
        } finally {
            IOUtils.closeQuietly(response);
        }
    }


    protected void handleResponse(Request request, String charset, Response response, Task task, Page page) throws IOException {
        String content = getResponseContent(charset, response);
        page.setRawText(content);
        page.setUrl(new PlainText(request.getUrl()));
        page.setRequest(request);
        page.setStatusCode(response.code());
        page.setDownloadSuccess(true);
        page.setHeaders(response.headers().toMultimap());
    }

    private String getResponseContent(String charset, Response response) throws IOException {

        ResponseBody responseBody = response.body();

        if (null == responseBody) {
            return "";
        }

        if (charset == null) {
            byte[] contentBytes = responseBody.bytes();
            String htmlCharset = getHtmlCharset(response, contentBytes);
            if (htmlCharset != null) {
                return new String(contentBytes, htmlCharset);
            } else {
                log.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
                return new String(contentBytes);
            }
        } else {
            return responseBody.string();
        }
    }

    private String getHtmlCharset(Response response, byte[] contentBytes) throws IOException {
        String contentType = response.header("Content-Type");
        return CharsetUtils.detectCharset(contentType == null ? "" : contentType, contentBytes);
    }

    @Override
    public void setThread(int threadNum) {

    }

    public OkHttpDownloader setProxySelector(ProxySelector proxySelector) {
        this.client = build(proxySelector);
        return this;
    }


    private OkHttpClient build(ProxySelector proxySelector) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (null != proxySelector) {
            builder.proxySelector(proxySelector);
        }
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        builder.connectTimeout(30, TimeUnit.SECONDS);
        return builder.build();
    }
}
