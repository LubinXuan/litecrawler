package me.robin.crawler.common;

import lombok.Data;
import me.robin.crawler.crawlers.Param;

/**
 * Created by Lubin.Xuan on 2017-07-11.
 * {desc}
 */
@Data
public class SpiderConfig {
    Param.PlatName name;
    SpiderDefine[] spiders;

    @Data
    public static class SpiderDefine {
        String subType;
        String basePackage;
        String[] pageProcessors;
        boolean cookieUpdate;
        String charset;
        int threadNum;
        int cycleRetryTimes;
        int sleepTime;
        int maxSleepTime;
        String startUrl;
    }
}
