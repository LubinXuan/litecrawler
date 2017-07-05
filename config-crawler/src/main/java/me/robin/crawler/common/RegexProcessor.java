package me.robin.crawler.common;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.handler.SubPageProcessor;

import java.util.regex.Pattern;

/**
 * Created by LubinXuan on 2017/7/4.
 */
public abstract class RegexProcessor implements SubPageProcessor {

    private Pattern pattern;

    public RegexProcessor(String urlPattern) {
        pattern = Pattern.compile(urlPattern);
    }

    @Override
    public boolean match(Request page) {
        return pattern.matcher(page.getUrl()).matches();
    }
}
