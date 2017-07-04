package me.robin.crawler.p2peye;

import me.robin.crawler.BaseMatchPageProcessor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformListPageProcessor extends BaseMatchPageProcessor {

    public PlatformListPageProcessor() {
        super("http://www.p2peye.com/platform/all/");
    }

    @Override
    public MatchOther processPage(Page page) {
        Document document = Jsoup.parse(page.getRawText(),page.getRequest().getUrl());
        Elements nextPage = document.select("div.c-page a:containsOwn(下一页)");
        if (null != nextPage) {
            page.addTargetRequest(nextPage.attr("abs:href"));
        }

        Elements platforms = document.select("#c-pfinfo>li");
        for (Element platform:platforms) {
            Map<String, String> extra = new HashMap<>();
            //extra.put(Const.platformName,platform.select("a.c-pfname").text());

            platform.select("a.c-pflabeler").text();
        }
        return MatchOther.NO;
    }
}
