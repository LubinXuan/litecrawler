package me.robin.crawler.p2peye;

import me.robin.crawler.common.BaseMatchPageProcessor;
import me.robin.crawler.Param;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.HtmlNode;
import us.codecraft.webmagic.selector.Selectable;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformListPageProcessor extends BaseMatchPageProcessor {

    public PlatformListPageProcessor() {
        super("http://www.p2peye.com/platform/all/");
    }

    @Override
    public MatchOther processPage(Page page) {
        String nextUrl = page.getHtml().$("div.c-page a:containsOwn(下一页)", "abs:href").get();
        if (StringUtils.isNotBlank(nextUrl)) {
            page.addTargetRequest(nextUrl);
        }
        HtmlNode platforms = (HtmlNode) page.getHtml().$("li.ui-result-item");
        for (Selectable platform : platforms.nodes()) {
            Request request = new Request(platform.$("a.ui-result-pname", "href").get());
            request.putExtra(Param.plat.name, platform.$("a.ui-result-pname", "text"));
        }
        return MatchOther.NO;
    }
}
