package me.robin.crawler;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import me.robin.crawler.common.*;
import me.robin.crawler.crawlers.p2peye.utils.CookieUpdater;
import me.robin.crawler.crawlers.p2peye.utils.HttpDownloader;
import me.robin.crawler.web.SpiderManagerController;
import me.robin.crawler.web.util.SwaggerConfigure;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.handler.SubPageProcessor;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.monitor.SpiderStatus;
import us.codecraft.webmagic.pipeline.Pipeline;

import javax.annotation.PostConstruct;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Lubin.Xuan on 2017-07-11.
 * {desc}
 */
@SpringBootApplication
@Import(SwaggerConfigure.class)
@EnableScheduling
@Slf4j
public class SpiderManagerApplication implements ApplicationListener<ContextRefreshedEvent> {
    @Value("${spiders}")
    private String spiders;
    @Value("${chromeBin}")
    private String chromeBin;
    @Value("${push.host}")
    private String pushHost;
    @Value("${push.port}")
    private int pushPort;
    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;

    private AtomicBoolean spiderStart = new AtomicBoolean(false);

    private Map<String, Spider> spiderMap = new ConcurrentHashMap<>();

    private Map<String, SpiderStatus> statusMap = new ConcurrentHashMap<>();

    private Map<String, String> startUrlMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() throws Exception {

        DataPushPipeline.host(pushHost, pushPort);

        JedisPool jedisPool = new JedisPool(redisHost, redisPort);

        for (String spiderConfig : spiders.split("\\|")) {
            SpiderConfig config = JSON.parseObject(SpiderManagerController.class.getClassLoader().getResourceAsStream(spiderConfig), SpiderConfig.class);

            Pipeline pipeline = new DataPushPipeline(config.getName());
            BizSpider[] spiders = new BizSpider[config.getSpiders().length];
            for (int i = 0; i < config.getSpiders().length; i++) {
                SpiderConfig.SpiderDefine define = config.getSpiders()[i];

                if (define.getPageProcessors().length < 1) {
                    return;
                }

                String spiderName;

                Site site = Site.me();
                if (StringUtils.isNotBlank(define.getSubType())) {
                    spiderName = config.getName().getName() + "-" + define.getSubType();
                } else {
                    spiderName = config.getName().getName();
                }

                site.setDomain(spiderName);

                if (StringUtils.isNotBlank(define.getCharset())) {
                    site.setCharset(define.getCharset());
                }
                site.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.86 Safari/537.36");
                site.setSleepTime(define.getSleepTime() * 1000);
                site.setCycleRetryTimes(define.getCycleRetryTimes());

                CompositePageProcessor pageProcessor = new CompositePageProcessor(site);
                for (String processor : define.getPageProcessors()) {
                    Constructor<?> constructor = Class.forName(define.getBasePackage() + "." + processor).getConstructors()[0];
                    SubPageProcessor subPageProcessor;
                    if (constructor.getParameterCount() > 0) {
                        subPageProcessor = (SubPageProcessor) constructor.newInstance(spiders[i - 1]);
                    } else {
                        subPageProcessor = (SubPageProcessor) constructor.newInstance();
                    }
                    pageProcessor.addSubPageProcessor(subPageProcessor);

                    if (subPageProcessor instanceof SitePrepare) {
                        ((SitePrepare) subPageProcessor).prepare(site);
                    }
                }

                Downloader downloader;
                RedisPrioritySchedulerExt scheduler;
                if (define.isCookieUpdate()) {
                    CookieUpdater cookieUpdater = new CookieUpdater(chromeBin);
                    downloader = new HttpDownloader(cookieUpdater);
                    scheduler = new RedisPrioritySchedulerExt(jedisPool) {
                        @Override
                        public synchronized Request poll(Task task) {
                            cookieUpdater.waitCookieUpdate();
                            return super.poll(task);
                        }
                    };
                } else {
                    downloader = new OkHttpDownloader();
                    scheduler = new RedisPrioritySchedulerExt(jedisPool);
                }

                BizSpider spider = (BizSpider) BizSpider.create(pageProcessor).thread(define.getThreadNum());
                spider.setSpiderListeners(new ArrayList<>());
                spider.getSpiderListeners().add(new RateDynamicListener(spider, define.getSleepTime(), define.getMaxSleepTime()));

                SpiderMonitor.MonitorSpiderListener monitorSpiderListener = SpiderMonitor.instance().new MonitorSpiderListener();
                spider.getSpiderListeners().add(monitorSpiderListener);


                spider.addPipeline(pipeline);
                spider.addPipeline(scheduler);
                spider.setDownloader(downloader).setScheduler(scheduler);
                spider.setExitWhenComplete(false);
                statusMap.put(spiderName, new SpiderStatusExt(spider, monitorSpiderListener));
                spiderMap.put(spiderName, spider);
                spiders[i] = spider;
                if (StringUtils.isNotBlank(define.getStartUrl())) {
                    startUrlMap.put(spiderName, define.getStartUrl());
                }
            }
        }
    }

    public class SpiderStatusExt extends SpiderStatus {
        public SpiderStatusExt(Spider spider, SpiderMonitor.MonitorSpiderListener monitorSpiderListener) {
            super(spider, monitorSpiderListener);
        }

        public int getSleepTime() {
            return spider.getSite().getSleepTime();
        }
    }

    @Bean
    public Map<String, Spider> spiderMap() {
        return Collections.unmodifiableMap(spiderMap);
    }

    @Bean
    public Map<String, SpiderStatus> statusMap() {
        return Collections.unmodifiableMap(statusMap);
    }

    @Bean
    public Map<String, String> startUrlMap() {
        return Collections.unmodifiableMap(startUrlMap);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (spiderStart.compareAndSet(false, true)) {
            spiderMap.forEach((name, spider) -> {
                log.info("启动爬虫 {}", name);
                spider.start();
            });
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SpiderManagerApplication.class, args);
    }
}
