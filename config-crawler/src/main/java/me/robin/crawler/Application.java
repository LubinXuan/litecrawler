package me.robin.crawler;

import me.robin.crawler.wdzj.PlatformDetailProcessor;
import me.robin.crawler.wdzj.PlatformListPageProcessor;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.monitor.SpiderMonitor;

import javax.management.JMException;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class Application {
    public static void main(String[] args) throws JMException {
        CompositePageProcessor pageProcessor = new CompositePageProcessor(Site.me());
        pageProcessor.addSubPageProcessor(new PlatformListPageProcessor());
        pageProcessor.addSubPageProcessor(new PlatformDetailProcessor());
        Spider spider = Spider.create(pageProcessor).thread(5).addUrl("http://www.wdzj.com/front_select-plat?sort=0&currPage=1");
        spider.setExitWhenComplete(false);
        SpiderMonitor.instance().register(spider);
        spider.start();
    }
}
