package me.robin.crawler.wdzj;

import me.robin.crawler.Param;
import me.robin.crawler.util.RegexProcessor;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.CssSelector;
import us.codecraft.webmagic.selector.HtmlNode;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.utils.HttpConstant;

/**
 * Created by Lubin.Xuan on 2017-07-05.
 */
public class CommentProcessor extends RegexProcessor {

    private static final String url = "http://www.wdzj.com/front/dianpingInfo/{pid}/20/{page}";

    public static String commentUrl(String platId, int page) {
        return StringUtils.replaceEach(url, new String[]{"{pid}", "{page}"}, new String[]{platId, Integer.toString(page)});
    }

    public CommentProcessor() {
        super("http://www.wdzj.com/front/dianpingInfo/(\\d+?)/20/(\\d+?)");
    }

    @Override
    public MatchOther processPage(Page page) {
        HtmlNode htmlNode = (HtmlNode) page.getHtml().select(new CssSelector("ul.commentList div.bor"));
        for (Selectable selectable : htmlNode.nodes()) {
            String remark = selectable.$("div.commentFont p.font", "allText").get();
            String remarkTime = selectable.$("span.date", "text").get();
            String userName = selectable.$("span.name", "allText").get();
            String plat = (String) page.getRequest().getExtra(Param.comment.platname);
            page.putField(Param.comment.platname, plat);
            page.putField(Param.comment.remark, remark);
            page.putField(Param.comment.remarktime, remarkTime + " 00:00:00");
            page.putField(Param.comment.username, userName);
            page.putField(Param.source, Param.Plat.wdzj);
            page.putField(Param.dataType, Param.comment.class.getSimpleName());
        }

        String currentPage = page.getHtml().$("div.pageList a.on", "pagenumber").get();
        String nextPage = page.getHtml().$("div.pageList a:containsOwn(下一页)", "pagenumber").get();
        if (!StringUtils.equals(currentPage, nextPage)) {
            Request request = new Request(page.getHtml().$("div.pageList", "url") + nextPage);
            request.setMethod(HttpConstant.Method.POST);
            request.setExtras(page.getRequest().getExtras());
            request.addHeader("referer", page.getRequest().getHeaders().get("referer"));
            page.addTargetRequest(request);
        }
        return MatchOther.NO;
    }
}
