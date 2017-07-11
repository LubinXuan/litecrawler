package me.robin.crawler;

import com.alibaba.fastjson.JSON;
import me.robin.crawler.common.*;
import me.robin.crawler.crawlers.p2peye.utils.CookieUpdater;
import me.robin.crawler.crawlers.p2peye.utils.HttpDownloader;
import me.robin.crawler.web.SpiderManagerController;
import me.robin.crawler.web.util.SwaggerConfigure;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.handler.SubPageProcessor;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.monitor.SpiderStatus;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.scheduler.PriorityScheduler;
import us.codecraft.webmagic.scheduler.Scheduler;

import javax.annotation.PostConstruct;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lubin.Xuan on 2017-07-11.
 * {desc}
 */
@SpringBootApplication
@Import(SwaggerConfigure.class)
public class SpiderManagerApplication {

    @Value("${spiders}")
    private String spiders;

    @Value("${chromeBin}")
    private String chromeBin;

    private Map<String, Spider> spiderMap = new ConcurrentHashMap<>();

    private Map<String, SpiderStatus> statusMap = new ConcurrentHashMap<>();


    @PostConstruct
    private void init() throws Exception {

        KVStoreClient.host("127.0.0.1", 8080);
        DataPushPipeline.host("127.0.0.1", 8080);

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
                }

                Downloader downloader;
                Scheduler scheduler;
                if (define.isCookieUpdate()) {
                    CookieUpdater cookieUpdater = new CookieUpdater(chromeBin);
                    downloader = new HttpDownloader(cookieUpdater);
                    scheduler = new PriorityScheduler() {
                        @Override
                        public synchronized Request poll(Task task) {
                            cookieUpdater.waitCookieUpdate();
                            return super.poll(task);
                        }
                    };
                } else {
                    downloader = new HttpClientDownloader();
                    scheduler = new PriorityScheduler();
                }

                BizSpider spider = (BizSpider) BizSpider.create(pageProcessor).thread(define.getThreadNum());
                spider.setSpiderListeners(new ArrayList<>());
                spider.getSpiderListeners().add(new RateDynamicListener(spider, define.getSleepTime(), define.getMaxSleepTime()));

                SpiderMonitor.MonitorSpiderListener monitorSpiderListener = SpiderMonitor.instance().new MonitorSpiderListener();
                spider.getSpiderListeners().add(monitorSpiderListener);


                spider.addPipeline(pipeline);
                spider.setDownloader(downloader).setScheduler(scheduler);
                spider.setExitWhenComplete(false);
                SpiderMonitor.instance().register(spider);
                spider.start();

                statusMap.put(spiderName, new SpiderStatus(spider, monitorSpiderListener));
                spiderMap.put(spiderName, spider);
                spiders[i] = spider;
            }
        }
    }

    @Bean
    public Map<String, Spider> spiderMap() {
        return spiderMap;
    }

    @Bean
    public Map<String, SpiderStatus> statusMap() {
        return statusMap;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpiderManagerApplication.class, args);
    }
}