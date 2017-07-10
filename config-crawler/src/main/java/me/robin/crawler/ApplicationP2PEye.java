package me.robin.crawler;

import me.robin.crawler.common.BizSpider;
import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.RateDynamicListener;
import me.robin.crawler.p2peye.*;
import me.robin.crawler.p2peye.utils.ChromeDownloader;
import me.robin.crawler.p2peye.utils.CookieUpdater;
import me.robin.crawler.p2peye.utils.HttpDownloader;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
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

    private static final String chromeBin = "C:\\Users\\Administrator\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe";

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
        Site site = site(Param.PlatName.P2PEYE.getName() + "-list").setCharset("gbk");
        CompositePageProcessor pageProcessor = new CompositePageProcessor(site);
        pageProcessor.addSubPageProcessor(new PlatformHtmlListPageProcessor(commonSpider));
        Spider spider = BizSpider.create(pageProcessor).thread(1);
        //.addUrl("http://lu.p2peye.com/index/getPlatform")
        spider.addPipeline(pipeline);
        spider.setSpiderListeners(new ArrayList<>());
        spider.getSpiderListeners().add(new RateDynamicListener(spider, 5, 10));
        spider.setExitWhenComplete(false);

        CookieUpdater cookieUpdater = new CookieUpdater(chromeBin);

        spider.setScheduler(new PriorityScheduler() {
            @Override
            public synchronized Request poll(Task task) {
                cookieUpdater.waitCookieUpdate();
                return super.poll(task);
            }
        });
        spider.setDownloader(new HttpDownloader(cookieUpdater));
        spider.start();

        return spider;
    }

    public static Spider commonSpider(Pipeline pipeline) {
        Site site = site(Param.PlatName.P2PEYE.getName() + "-common").setCharset("utf-8");
        CompositePageProcessor pageProcessor = new CompositePageProcessor(site);
        pageProcessor.addSubPageProcessor(new PlatformListPageProcessor());
        pageProcessor.addSubPageProcessor(new PlatformDetailProcessor());
        pageProcessor.addSubPageProcessor(new PlatformShujuProcessor());
        pageProcessor.addSubPageProcessor(new CommentProcessor());

        ProductProcessors.addProcessor(pageProcessor);

        Spider spider = BizSpider.create(pageProcessor).thread(5);
        spider.addPipeline(pipeline);
        spider.setSpiderListeners(new ArrayList<>());
        spider.getSpiderListeners().add(new RateDynamicListener(spider, 2, 10));
        spider.setExitWhenComplete(false);
        CookieUpdater cookieUpdater = new CookieUpdater(chromeBin);

        spider.setScheduler(new PriorityScheduler() {
            @Override
            public synchronized Request poll(Task task) {
                cookieUpdater.waitCookieUpdate();
                return super.poll(task);
            }
        });
        spider.setDownloader(new HttpDownloader(cookieUpdater));

        spider.start();
        return spider;
    }
}
