package me.robin.crawler;

import me.robin.crawler.common.BizSpider;
import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.RateDynamicListener;
import me.robin.crawler.wdzj.*;
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
        spider.setSpiderListeners(new ArrayList<>());
        spider.getSpiderListeners().add(new RateDynamicListener(spider, 2, 10));
        spider.setExitWhenComplete(false);
        SpiderMonitor.instance().register(spider);
        spider.start();
    }
}
