package me.robin.crawler;

import me.robin.crawler.common.BizSpider;
import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.KVStoreClient;
import me.robin.crawler.common.RateDynamicListener;
import me.robin.crawler.p2peye.*;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.scheduler.PriorityScheduler;

import javax.management.JMException;
import java.util.ArrayList;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class ApplicationP2PEye extends BaseApplication {
    public static void main(String[] args) throws JMException {
        Pipeline pipeline = new DataPushPipeline(Param.PlatName.P2PEYE);
        Spider commonSpider = commonSpider(pipeline);
        Spider listSpider = listSpider(commonSpider, pipeline);

        listSpider.addUrl("http://www.p2peye.com/platform/all/");

        Request request = new Request("http://licai.p2peye.com/loans");
        request.setPriority(1);
        commonSpider.addRequest(request);

        SpiderMonitor.instance().register(commonSpider);
        SpiderMonitor.instance().register(listSpider);
    }


    private static Spider listSpider(Spider commonSpider, Pipeline pipeline) {
        Site site = Site.me();
        site.setDomain(Param.PlatName.P2PEYE.getName() + "-list");
        site.setSleepTime(4).setCharset("gbk");
        site.setCycleRetryTimes(4);
        CompositePageProcessor pageProcessor = new CompositePageProcessor(site);
        pageProcessor.addSubPageProcessor(new PlatformHtmlListPageProcessor(commonSpider));
        Spider spider = BizSpider.create(pageProcessor).thread(1);
        //.addUrl("http://lu.p2peye.com/index/getPlatform")
        spider.addPipeline(pipeline);
        spider.setSpiderListeners(new ArrayList<>());
        spider.getSpiderListeners().add(new RateDynamicListener(spider, 3, 10));
        spider.setExitWhenComplete(false);
        spider.setScheduler(new PriorityScheduler());
        spider.start();

        return spider;
    }

    public static Spider commonSpider(Pipeline pipeline) {
        Site site = Site.me();
        site.setDomain(Param.PlatName.P2PEYE.getName() + "-common");
        site.setSleepTime(4).setCharset("UTF-8");
        site.setCycleRetryTimes(4);
        CompositePageProcessor pageProcessor = new CompositePageProcessor(site);
        pageProcessor.addSubPageProcessor(new PlatformListPageProcessor());
        pageProcessor.addSubPageProcessor(new PlatformDetailProcessor());
        pageProcessor.addSubPageProcessor(new PlatformShujuProcessor());
        pageProcessor.addSubPageProcessor(new CommentProcessor());

        ProductProcessors.addProcessor(pageProcessor);

        Spider spider = BizSpider.create(pageProcessor).thread(5);
        spider.addPipeline(pipeline);
        spider.setSpiderListeners(new ArrayList<>());
        spider.getSpiderListeners().add(new RateDynamicListener(spider, 3, 10));
        spider.setExitWhenComplete(false);
        spider.setScheduler(new PriorityScheduler());
        spider.start();
        return spider;
    }
}
