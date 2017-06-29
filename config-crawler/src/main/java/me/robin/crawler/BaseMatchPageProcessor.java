package me.robin.crawler;

import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.handler.SubPageProcessor;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public abstract class BaseMatchPageProcessor implements SubPageProcessor {

    protected String urlBase;

    public BaseMatchPageProcessor(String urlBase) {
        this.urlBase = urlBase;
    }

    @Override
    public boolean match(Request request) {
        return StringUtils.startsWith(request.getUrl(), urlBase);
    }
}
