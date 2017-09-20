package me.robin.crawler.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by Lubin.Xuan on 2017-09-20.
 * {desc}
 */
@Component
@Slf4j
public class SpiderStartUrlScheduleJob {
    @Resource
    private Map<String, Spider> spiderMap;

    @Resource
    private Map<String, String> startUrlMap;

    @Scheduled(cron = "0 0 6/* * * ?")
    public void pushStartUrl() {
        startUrlMap.forEach((name, url) -> {
            Spider spider = spiderMap.get(name);
            if (null != spider) {
                log.info("加入种子链接:{} {}", name, url);
                spider.addUrl(url);
            }
        });
    }
}
