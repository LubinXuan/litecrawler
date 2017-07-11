package me.robin.crawler.crawlers.wdzj;

import me.robin.crawler.common.BaseMatchPageProcessor;
import me.robin.crawler.crawlers.Param;
import me.robin.crawler.common.CralwData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
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
        String value = page.getHtml().selectDocument(new CssSelector("div.cen-zk", "allText"));
        page.getRequest().putExtra(Param.plat.instruction, value);

        Request request = new Request(PlatformAssetsTypeProcessor.url + page.getHtml().$("#platId","value") + ".html");
        request.setExtras(page.getRequest().getExtras());
        request.addHeader("referer", page.getRequest().getHeaders().get("referer"));
        request.setPriority(1);
        page.addTargetRequest(request);
        page.getResultItems().setSkip(true);
        return MatchOther.NO;
    }
}
