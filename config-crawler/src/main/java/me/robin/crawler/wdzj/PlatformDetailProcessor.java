package me.robin.crawler.wdzj;

import me.robin.crawler.BaseMatchPageProcessor;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformDetailProcessor extends BaseMatchPageProcessor{

    public static final String url = "http://wwwservice.wdzj.com/api/plat/platData30Days?platId=";

    public PlatformDetailProcessor() {
        super(url);
    }

    @Override
    public MatchOther processPage(Page page) {
        System.out.println();
        return null;
    }
}
