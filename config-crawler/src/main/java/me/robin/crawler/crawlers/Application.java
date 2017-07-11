package me.robin.crawler.crawlers;

import me.robin.crawler.common.BizSpider;
import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.RateDynamicListener;
import me.robin.crawler.crawlers.p2peye.CommentProcessor;
import me.robin.crawler.crawlers.p2peye.PlatformDetailProcessor;
import me.robin.crawler.crawlers.p2peye.PlatformListPageProcessor;
import me.robin.crawler.crawlers.wdzj.PlatformAssetsTypeProcessor;
import me.robin.crawler.crawlers.wdzj.PlatformDetailHtmlProcessor;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.monitor.SpiderMonitor;

import javax.management.JMException;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class Application extends BaseApplication {
    public static void main(String[] args) throws JMException {
        Site site = site(Param.PlatName.WDZJ.getName());
        CompositePageProcessor pageProcessor = new CompositePageProcessor(site);
        pageProcessor.addSubPageProcessor(new PlatformListPageProcessor());
        pageProcessor.addSubPageProcessor(new PlatformDetailProcessor());
        pageProcessor.addSubPageProcessor(new PlatformAssetsTypeProcessor());
        pageProcessor.addSubPageProcessor(new PlatformDetailHtmlProcessor());
        pageProcessor.addSubPageProcessor(new CommentProcessor());
        Spider spider = BizSpider.create(pageProcessor)
                .thread(5).addUrl("http://www.wdzj.com/front_select-plat?sort=0&currPage=1");
        spider.addPipeline(new DataPushPipeline(Param.PlatName.WDZJ));
        dynamicListener(spider);
        spider.setExitWhenComplete(false);
        SpiderMonitor.instance().register(spider);
        spider.start();
    }
}
