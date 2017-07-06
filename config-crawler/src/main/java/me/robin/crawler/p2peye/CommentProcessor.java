package me.robin.crawler.p2peye;

import me.robin.crawler.Param;
import me.robin.crawler.common.CralwData;
import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.RegexProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.HtmlNode;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lubin.Xuan on 2017-07-06.
 * {desc}
 */
public class CommentProcessor extends RegexProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CommentProcessor.class);

    public CommentProcessor() {
        super("http://(.*?).p2peye.com/comment/(list-\\d+?-\\d+?-\\d+?\\.html)?");
    }

    @Override
    public MatchOther processPage(Page page) {
        HtmlNode comments = (HtmlNode) page.getHtml().$("li[id^=comment_item_]");

        Integer commentLimit = (Integer) page.getRequest().getExtra(Param.comment_id_limit);
        Integer commentCrawled = (Integer) page.getRequest().getExtra(Param.comment_crawled);
        if (null == commentCrawled) {
            commentCrawled = 0;
        }

        List<Map<String, Object>> commentList = new ArrayList<>();
        for (Selectable comment : comments.nodes()) {
            int id = Integer.parseInt(StringUtils.replace(comment.$("li", "id").get(), "comment_item_", ""));
            if (null != commentLimit && id <= commentLimit) {
                break;
            }
            Map<String, Object> data = CralwData.commentData();
            data.put(Param.comment.platname, page.getRequest().getExtra(Param.comment.platname));
            data.put(Param.comment.remark, comment.$("div.comment", "allText").get());
            data.put(Param.comment.remarktime, comment.$("div.time", "allText").get());
            data.put(Param.comment.username, comment.$("a.username", "allText").get());
            data.put(Param.comment.praise, comment.$("div.commentcore", "allText").get());
            commentList.add(data);
            commentCrawled++;
        }//下一页

        page.putField(DataPushPipeline.DATA_LIST, commentList);

        String nextPage = page.getHtml().$("div.ui-pagenav a:containsOwn(下一页)", "abs:href").get();
        if (StringUtils.isNotBlank(nextPage)) {
            Request request = new Request(nextPage);
            request.setMethod(HttpConstant.Method.GET);
            request.setExtras(page.getRequest().getExtras());
            request.putExtra(Param.comment_crawled, commentCrawled);
            page.addTargetRequest(request);
        } else {
            logger.info("评论爬取完成,共爬取评论数;{}   <-{}", commentCrawled, page.getRequest().getUrl());
        }
        return MatchOther.NO;
    }
}
