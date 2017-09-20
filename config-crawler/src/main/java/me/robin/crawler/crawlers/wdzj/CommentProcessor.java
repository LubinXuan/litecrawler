package me.robin.crawler.crawlers.wdzj;

import com.alibaba.fastjson.util.TypeUtils;
import me.robin.crawler.common.SitePrepare;
import me.robin.crawler.crawlers.Param;
import me.robin.crawler.common.CralwData;
import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.RegexProcessor;
import me.robin.crawler.crawlers.wdzj.utils.LoginUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.selector.CssSelector;
import us.codecraft.webmagic.selector.HtmlNode;
import us.codecraft.webmagic.selector.Selectable;
import us.codecraft.webmagic.utils.HttpConstant;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lubin.Xuan on 2017-07-05.
 */
public class CommentProcessor extends RegexProcessor implements SitePrepare {

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
        Integer commentLimit = TypeUtils.castToInt(page.getRequest().getExtra(Param.cursor_limit));
        Integer commentCrawled = (Integer) page.getRequest().getExtra(Param.comment_crawled);
        Integer updateCursor = (Integer) page.getRequest().getExtra(Param.cursor_limit_save);
        if (null == commentCrawled) {
            commentCrawled = 0;
        }
        String platId = StringUtils.substringBetween(page.getRequest().getUrl(), "dianpingInfo/", "/20/");
        List<Map<String, Object>> commentList = new ArrayList<>();
        for (Selectable comment : htmlNode.nodes()) {
            int id = Integer.parseInt(StringUtils.replace(comment.$("span[id^=useful_]", "id").get(), "useful_", ""));
            if (null != commentLimit && id <= commentLimit) {
                break;
            }

            String plat = (String) page.getRequest().getExtra(Param.comment.platname);
            if (null == updateCursor || updateCursor < id) {
                updateCursor = id;
                page.putField(Param.cursor_limit_save, id);
                page.putField(Param.cursor_limit_update, true);
                page.putField(Param.cursor_limit_key, plat);
            }
            String remark = comment.$("div.commentFont p.font", "allText").get();
            String remarkTime = comment.$("span.date", "text").get();
            String userName = comment.$("span.name", "allText").get();

            Map<String, Object> commentMap = CralwData.commentData();
            commentMap.put(Param.comment.platname, plat);
            commentMap.put(Param.comment.remark, remark);
            commentMap.put(Param.comment.remarktime, remarkTime + " 00:00:00");
            commentMap.put(Param.comment.username, userName);
            commentMap.put(Param.comment.headimg, comment.$("div.avatar img", "src").get());
            commentMap.put(Param.comment.useful, comment.$("#useful_" + id, "text").get());
            commentMap.put(Param.dataUid, platId + "-" + id);
            String praise = StringUtils.trim(comment.$("span.tags", "text").get());
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
            request.putExtra(Param.cursor_limit_save, updateCursor);
            request.addHeader("referer", page.getRequest().getHeaders().get("referer"));
            page.addTargetRequest(request);
        } else {
            logger.info("评论爬取完成,共爬取评论数;{}   <-{}  {}", commentCrawled, commentLimit, page.getRequest().getHeaders().get("referer"));
        }
        return MatchOther.NO;
    }

    @Override
    public void prepare(Site site) {
        Header[] headers = LoginUtil.login("18258837523", "1QaZ2WsX");
        if (null != headers) {
            for (Header header : headers) {
                List<HttpCookie> cookieList = HttpCookie.parse(header.getValue());
                cookieList.forEach(cookie -> {
                    if (null == cookie.getDomain()) {
                        site.addCookie(".wdzj.com", cookie.getName(), cookie.getValue());
                    }else{
                        site.addCookie(cookie.getDomain(), cookie.getName(), cookie.getValue());
                    }

                });
            }
            logger.info("登陆成功");
        }
    }
}
