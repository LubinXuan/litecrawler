package me.robin.crawler.wdzj;

import me.robin.crawler.Param;
import me.robin.crawler.common.CralwData;
import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.RegexProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.CssSelector;
import us.codecraft.webmagic.selector.HtmlNode;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lubin.Xuan on 2017-07-05.
 */
public class CommentProcessor extends RegexProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CommentProcessor.class);

    private static final String url = "http://www.wdzj.com/front/dianpingInfo/{pid}/20/{page}";

    private static final Map<String, String> PRAISE_ALIAS = new HashMap<>();

    static {
        PRAISE_ALIAS.put("推荐", "好评");
        PRAISE_ALIAS.put("不推荐", "差评");
    }

    public static String commentUrl(String platId, int page) {
        return StringUtils.replaceEach(url, new String[]{"{pid}", "{page}"}, new String[]{platId, Integer.toString(page)});
    }

    public CommentProcessor() {
        super("http://www.wdzj.com/front/dianpingInfo/(\\d+?)/20/(\\d+?)");
    }

    @Override
    public MatchOther processPage(Page page) {
        HtmlNode htmlNode = (HtmlNode) page.getHtml().select(new CssSelector("ul.commentList div.bor"));
        Integer commentLimit = (Integer) page.getRequest().getExtra(Param.cursor_limit);
        Integer commentCrawled = (Integer) page.getRequest().getExtra(Param.comment_crawled);
        if (null == commentCrawled) {
            commentCrawled = 0;
        }
        List<Map<String, Object>> commentList = new ArrayList<>();
        for (Selectable selectable : htmlNode.nodes()) {
            int id = Integer.parseInt(StringUtils.replace(selectable.$("span[id^=useful_]", "id").get(), "useful_", ""));
            if (null != commentLimit && id <= commentLimit) {
                break;
            }
            String remark = selectable.$("div.commentFont p.font", "allText").get();
            String remarkTime = selectable.$("span.date", "text").get();
            String userName = selectable.$("span.name", "allText").get();
            String plat = (String) page.getRequest().getExtra(Param.comment.platname);
            Map<String, Object> commentMap = CralwData.commentData();
            commentMap.put(Param.comment.platname, plat);
            commentMap.put(Param.comment.remark, remark);
            commentMap.put(Param.comment.remarktime, remarkTime + " 00:00:00");
            commentMap.put(Param.comment.username, userName);
            String praise = StringUtils.trim(selectable.$("span.tags", "text").get());
            if (StringUtils.isBlank(praise)) {
                praise = "一般";
            }
            commentMap.put(Param.comment.praise, PRAISE_ALIAS.getOrDefault(praise, praise));
            commentList.add(commentMap);
            commentCrawled++;
        }
        page.putField(DataPushPipeline.DATA_LIST, commentList);

        String currentPage = page.getHtml().$("div.pageList a.on", "pagenumber").get();
        String nextPage = page.getHtml().$("div.pageList a:containsOwn(下一页)", "pagenumber").get();
        if (!StringUtils.equals(currentPage, nextPage)) {
            Request request = new Request(page.getHtml().$("div.pageList", "url") + nextPage);
            request.setMethod(HttpConstant.Method.POST);
            request.setExtras(page.getRequest().getExtras());
            request.putExtra(Param.comment_crawled, commentCrawled);
            request.addHeader("referer", page.getRequest().getHeaders().get("referer"));
            page.addTargetRequest(request);
        } else {
            logger.info("评论爬取完成,共爬取评论数;{}   <-{}", commentCrawled, page.getRequest().getUrl());
        }
        return MatchOther.NO;
    }
}
