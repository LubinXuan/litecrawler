package me.robin.crawler.common;

import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by LubinXuan on 2017/7/6.
 */
public class BizSpider extends Spider {

    private static class NameThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NameThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            if (StringUtils.isBlank(name)) {
                namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
            } else {
                namePrefix = name + "-thread-";
            }
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    public BizSpider(PageProcessor pageProcessor) {
        super(pageProcessor);
        setExecutorService(Executors.newCachedThreadPool(new NameThreadFactory(pageProcessor.getSite().getDomain())));
    }

    public static Spider create(PageProcessor pageProcessor) {
        return new BizSpider(pageProcessor);
    }
}
