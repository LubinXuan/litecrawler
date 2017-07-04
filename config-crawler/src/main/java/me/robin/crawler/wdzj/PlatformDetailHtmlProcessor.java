package me.robin.crawler.wdzj;

import me.robin.crawler.BaseMatchPageProcessor;
import me.robin.crawler.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.CssSelector;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformDetailHtmlProcessor extends BaseMatchPageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PlatformDetailHtmlProcessor.class);

    public static final String url = "http://www.wdzj.com/dangan/";

    public PlatformDetailHtmlProcessor() {
        super("http://www.wdzj.com/dangan/");
    }

    @Override
    public MatchOther processPage(Page page) {
        String value = page.getHtml().selectDocument(new CssSelector("div.cen-zk","allText"));
        page.getRequest().getExtras().put(Param.plat.instruction, value);
        return MatchOther.NO;
    }
}
