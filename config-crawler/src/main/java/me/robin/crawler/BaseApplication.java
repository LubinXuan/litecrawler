package me.robin.crawler;

import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.KVStoreClient;
import me.robin.crawler.common.RateDynamicListener;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by LubinXuan on 2017/6/3.
 */
abstract class BaseApplication {
    static {
        KVStoreClient.host("127.0.0.1", 8080);
        DataPushPipeline.host("127.0.0.1", 8080);
    }

    static Site site(String domain) {
        Site site = Site.me();
        site.setDomain(domain);
        site.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36");
        site.setSleepTime(1000);
        site.setCycleRetryTimes(4);
        return site;
    }

    static void dynamicListener(Spider spider) {
        List<SpiderListener> spiderListenerList = new ArrayList<>();
        if (null != spider.getSpiderListeners()) {
            spiderListenerList.addAll(spider.getSpiderListeners());
        }
        spiderListenerList.add(new RateDynamicListener(spider, 1, 10));
        spider.setSpiderListeners(spiderListenerList);
    }
}
