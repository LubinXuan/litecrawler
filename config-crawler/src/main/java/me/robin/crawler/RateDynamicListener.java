package me.robin.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by Lubin.Xuan on 2017-07-05.
 * 爬取速度调整
 */
public class RateDynamicListener implements SpiderListener {

    private final Spider spider;

    private final Logger logger;

    private final int minSleepTime;
    private final int maxSleepTime;

    private Set<String> errorUrls = new ConcurrentSkipListSet<>();

    public RateDynamicListener(Spider spider, int minSleepTime, int maxSleepTime) {
        this.spider = spider;
        this.maxSleepTime = maxSleepTime;
        this.minSleepTime = minSleepTime;
        this.logger = LoggerFactory.getLogger(spider.getSite().getDomain());
    }

    public RateDynamicListener(Spider spider) {
        this(spider, 3, 10);
    }

    @Override
    public void onSuccess(Request request) {
        logger.debug("页面[{}]下载成功", request.getUrl());
        if (errorUrls.remove(request.getUrl())) {
            synchronized (this) {
                if (spider.getSite().getSleepTime() <= minSleepTime) {
                    spider.getSite().setSleepTime(minSleepTime);
                } else {
                    spider.getSite().setSleepTime(spider.getSite().getSleepTime() - 1);
                }
            }
        }
    }

    @Override
    public void onError(Request request) {
        errorUrls.add(request.getUrl());
        synchronized (this) {
            if (spider.getSite().getSleepTime() < maxSleepTime) {
                spider.getSite().setSleepTime(spider.getSite().getSleepTime() + 1);
            }
        }
    }
}
