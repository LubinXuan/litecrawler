package me.robin.crawler;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderListener;

/**
 * Created by Lubin.Xuan on 2017-07-05.
 */
public class RetryListener implements SpiderListener {

    public static final String RETRY = "__retry";

    private final Spider spider;

    private final int maxRetry;

    private final Logger logger;

    public RetryListener(Spider spider, int maxRetry) {
        this.spider = spider;
        this.maxRetry = maxRetry;
        this.logger = LoggerFactory.getLogger(spider.getSite().getDomain());
    }

    @Override
    public void onSuccess(Request request) {
        logger.debug("页面[{}]下载成功", request.getUrl());
    }

    @Override
    public void onError(Request request) {
        int retry = MapUtils.getIntValue(request.getExtras(), RETRY, 0);
        if (this.maxRetry > retry) {
            request.putExtra(RETRY, retry + 1);
            spider.addRequest(request);
        } else {
            logger.warn("任务重试次数达到最大,放弃重试 {}", request.getUrl());
        }
    }
}
