package me.robin.crawler;

import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.KVStoreClient;
import us.codecraft.webmagic.Site;

import java.util.concurrent.TimeUnit;

/**
 * Created by LubinXuan on 2017/6/3.
 */
abstract class BaseApplication {
    static {
        KVStoreClient.host("127.0.0.1", 8080);
        DataPushPipeline.host("127.0.0.1", 8080);
    }

    public static Site site(String domain) {
        Site site = Site.me();
        site.setDomain(domain);
        site.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36");
        site.setSleepTime(2000);
        site.setCycleRetryTimes(4);
        return site;
    }
}
