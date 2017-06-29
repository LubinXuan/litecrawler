package me.robin.crawler;

import me.robin.crawler.p2peye.PlatformListPageProcessor;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.monitor.SpiderMonitor;

import javax.management.JMException;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class ApplicationP2PEye {
    public static void main(String[] args) throws JMException {
        CompositePageProcessor pageProcessor = new CompositePageProcessor(Site.me());
        pageProcessor.addSubPageProcessor(new PlatformListPageProcessor());
        Spider spider = Spider.create(pageProcessor).thread(5).addUrl("http://www.p2peye.com/platform/all/");
        spider.setExitWhenComplete(false);
        SpiderMonitor.instance().register(spider);
        spider.start();
    }
}
