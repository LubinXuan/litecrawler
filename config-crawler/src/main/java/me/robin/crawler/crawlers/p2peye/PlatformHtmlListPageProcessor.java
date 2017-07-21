package me.robin.crawler.crawlers.p2peye;

import me.robin.crawler.crawlers.Param;
import me.robin.crawler.common.BaseMatchPageProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.selector.HtmlNode;
import us.codecraft.webmagic.selector.Selectable;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformHtmlListPageProcessor extends BaseMatchPageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PlatformHtmlListPageProcessor.class);

    private final Spider commonSpider;

    public PlatformHtmlListPageProcessor(Spider commonSpider) {
        super("http://www.p2peye.com/platform/all/");
        this.commonSpider = commonSpider;
    }

    @Override
    public MatchOther processPage(Page page) {
        HtmlNode platforms = (HtmlNode) page.getHtml().$("a.ui-result-pname");
        for (Selectable platform : platforms.nodes()) {
            String url = platform.$("a", "href").get();
            if (StringUtils.startsWith(url, "http://.p2peye.com")) {
                logger.warn("平台数据异常:{}", platform);
            } else {
                Request request = new Request(url);
                request.putExtra(Param.plat.name, platform.$("a", "title").get());
                String location = StringUtils.substringAfter(platform.$("p.ui-result-address").get(), "注册地：");
                String[] _loc = StringUtils.split(location, "|", 2);
                if (null != _loc) {
                    if (_loc.length > 0) {
                        request.putExtra(Param.plat.province, _loc[0]);
                    }
                    if (_loc.length > 1) {
                        request.putExtra(Param.plat.city, _loc[1]);
                    }
                }
                commonSpider.addRequest(request);
            }
        }

        String nextUrl = page.getHtml().$("a[title=下一页]", "abs:href").get();
        if (StringUtils.isNotBlank(nextUrl)) {
            Request request = new Request(nextUrl);
            request.addHeader("referer", page.getRequest().getUrl());
            page.addTargetRequest(request);
        } else {
            logger.info("网贷天眼平台列表页抓取完毕");
        }
        page.getResultItems().setSkip(true);
        return MatchOther.NO;
    }
}
