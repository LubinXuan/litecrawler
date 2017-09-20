package me.robin.crawler.crawlers.p2peye;

import me.robin.crawler.crawlers.Param;
import me.robin.crawler.common.KVStoreClient;
import me.robin.crawler.common.RegexProcessor;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformDetailProcessor extends RegexProcessor {

    public PlatformDetailProcessor() {
        super("http://(.*?).p2peye.com");
    }

    @Override
    public MatchOther processPage(Page page) {
        Request request = new Request(page.getRequest().getUrl() + "/shuju/?&type=seven_day");
        request.addHeader("Referer", page.getRequest().getUrl() + "/shuju/");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.putExtra(Param.plat.name, page.getRequest().getExtra(Param.plat.name));
        request.putExtra(Param.plat.homelink, page.getHtml().$("a.pt_url", "data-href").get());
        request.putExtra(Param.plat.logo, page.getHtml().$("a.lo img", "src").get());
        String time = page.getHtml().$("div:containsOwn(上线时间)", "text").get();
        //上线时间：2012年01月22日
        time = StringUtils.replaceEach(StringUtils.substringAfter(time, "："), new String[]{"年", "月", "日"}, new String[]{"-", "-", ""});
        request.putExtra(Param.plat.onlinetime, time + " 00:00:00");
        request.putExtra(Param.plat.instruction, page.getHtml().$("#pingtaijianjie", "allText").get());
        String assetsType = page.getHtml().$("tr:contains(项目类型) td span", "allText").get();
        request.putExtra(Param.plat.assetstype, StringUtils.replace(assetsType, " | ", "|"));
        request.putExtra(Param.plat.location, page.getHtml().$("a.address", "allText").get());

        String platId = page.getHtml().$("#template-plat-id", "value").get();
        request.putExtra(Param.dataUid, platId);
        request.setPriority(1);
        page.addTargetRequest(request);
        page.getResultItems().setSkip(true);

        String count = page.getHtml().$("a[title=口碑]", "text").get();
        if (!StringUtils.equals(count, "口碑")) {
            request = new Request(page.getRequest().getUrl() + "/comment/");
            request.putExtra(Param.comment.platname, page.getRequest().getExtra(Param.plat.name));
            request.putExtra(Param.cursor_limit, KVStoreClient.get(Param.PlatName.P2PEYE.name(), (String) page.getRequest().getExtra(Param.plat.name)));
            request.setPriority(1);
            page.addTargetRequest(request);
        }
        return MatchOther.NO;
    }
}
