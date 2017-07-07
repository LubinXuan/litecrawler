package me.robin.crawler;

import me.robin.crawler.common.BizSpider;
import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.RateDynamicListener;
import me.robin.crawler.wdzj.CommentProcessor;
import me.robin.crawler.wdzj.PlatformDetailHtmlProcessor;
import me.robin.crawler.wdzj.PlatformDetailProcessor;
import me.robin.crawler.wdzj.PlatformListPageProcessor;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.monitor.SpiderMonitor;

import javax.management.JMException;
import java.util.ArrayList;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class Application extends BaseApplication {
    public static void main(String[] args) throws JMException {
        Site site = Site.me();
        site.setDomain(Param.PlatName.WDZJ.getName());
        site.setSleepTime(4);
        site.setCycleRetryTimes(4);
        CompositePageProcessor pageProcessor = new CompositePageProcessor(site);
        pageProcessor.addSubPageProcessor(new PlatformListPageProcessor());
        pageProcessor.addSubPageProcessor(new PlatformDetailProcessor());
        pageProcessor.addSubPageProcessor(new PlatformDetailHtmlProcessor());
        pageProcessor.addSubPageProcessor(new CommentProcessor());
        Spider spider = BizSpider.create(pageProcessor)
                .thread(5).addUrl("http://www.wdzj.com/front_select-plat?sort=0&currPage=1");
        spider.addPipeline(new DataPushPipeline(Param.PlatName.WDZJ));
        spider.setSpiderListeners(new ArrayList<>());
        spider.getSpiderListeners().add(new RateDynamicListener(spider, 3, 10));
        spider.setExitWhenComplete(false);
        SpiderMonitor.instance().register(spider);
        spider.start();
    }
}
