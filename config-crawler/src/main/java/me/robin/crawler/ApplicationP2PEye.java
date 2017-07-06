package me.robin.crawler;

import me.robin.crawler.common.BizSpider;
import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.RateDynamicListener;
import me.robin.crawler.p2peye.CommentProcessor;
import me.robin.crawler.p2peye.PlatformDetailProcessor;
import me.robin.crawler.p2peye.PlatformListPageProcessor;
import me.robin.crawler.p2peye.PlatformShujuProcessor;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.scheduler.PriorityScheduler;

import javax.management.JMException;
import java.util.ArrayList;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class ApplicationP2PEye {
    public static void main(String[] args) throws JMException {
        Site site = Site.me();
        site.setDomain(Param.PlatName.P2PEYE.getName());
        site.setSleepTime(4).setCharset("UTF-8");
        site.setCycleRetryTimes(4);
        CompositePageProcessor pageProcessor = new CompositePageProcessor(site);
        pageProcessor.addSubPageProcessor(new PlatformListPageProcessor());
        pageProcessor.addSubPageProcessor(new PlatformDetailProcessor());
        pageProcessor.addSubPageProcessor(new PlatformShujuProcessor());
        pageProcessor.addSubPageProcessor(new CommentProcessor());
        Spider spider = BizSpider.create(pageProcessor).thread(5).addUrl("http://lu.p2peye.com/index/getPlatform");
        spider.addPipeline(new DataPushPipeline(Param.PlatName.P2PEYE));
        spider.setSpiderListeners(new ArrayList<>());
        spider.getSpiderListeners().add(new RateDynamicListener(spider, 3, 10));
        spider.setExitWhenComplete(false);
        spider.setScheduler(new PriorityScheduler());
        SpiderMonitor.instance().register(spider);
        spider.start();
    }
}
