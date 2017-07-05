package me.robin.crawler;

import me.robin.crawler.p2peye.PlatformDetailProcessor;
import me.robin.crawler.p2peye.PlatformListPageProcessor;
import me.robin.crawler.p2peye.PlatformShujuProcessor;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.scheduler.PriorityScheduler;

import javax.management.JMException;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class ApplicationP2PEye {
    public static void main(String[] args) throws JMException {
        CompositePageProcessor pageProcessor = new CompositePageProcessor(Site.me());
        pageProcessor.addSubPageProcessor(new PlatformListPageProcessor());
        pageProcessor.addSubPageProcessor(new PlatformDetailProcessor());
        pageProcessor.addSubPageProcessor(new PlatformShujuProcessor());
        Spider spider = Spider.create(pageProcessor).thread(5).addUrl("http://lu.p2peye.com/index/getPlatform");
        spider.setExitWhenComplete(false);
        spider.setScheduler(new PriorityScheduler());
        SpiderMonitor.instance().register(spider);
        spider.start();
    }
}
