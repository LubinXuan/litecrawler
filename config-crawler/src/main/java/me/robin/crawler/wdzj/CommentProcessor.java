package me.robin.crawler.wdzj;

import com.alibaba.fastjson.util.TypeUtils;
import me.robin.crawler.Param;
import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.RegexProcessor;
import org.apache.commons.lang3.StringUtils;
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

    private static final String url = "http://www.wdzj.com/front/dianpingInfo/{pid}/20/{page}";

    private static final Map<String, String> PRAISE_ALIAS = new HashMap<>();

    static {
        PRAISE_ALIAS.put("推荐", "好");
        PRAISE_ALIAS.put("不推荐", "差");
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
        Integer commentLimit = (Integer) page.getRequest().getExtra(Param.comment_id_limit);
        List<Map<String, Object>> commentList = new ArrayList<>();
        for (Selectable selectable : htmlNode.nodes()) {
            int id = Integer.parseInt(StringUtils.replace(selectable.$("span[id^=useful_]", "id").get(),"useful_",""));
            if (null != commentLimit && id <= commentLimit) {
                break;
            }
            String remark = selectable.$("div.commentFont p.font", "allText").get();
            String remarkTime = selectable.$("span.date", "text").get();
            String userName = selectable.$("span.name", "allText").get();
            String plat = (String) page.getRequest().getExtra(Param.comment.platname);
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put(Param.comment.platname, plat);
            commentMap.put(Param.comment.remark, remark);
            commentMap.put(Param.comment.remarktime, remarkTime + " 00:00:00");
            commentMap.put(Param.comment.username, userName);
            commentMap.put(Param.source, Param.PlatName.wdzj);
            commentMap.put(Param.dataType, Param.comment.class.getSimpleName());
            String praise = StringUtils.trim(selectable.$("span.tags", "text").get());
            if (StringUtils.isBlank(praise)) {
                praise = "一般";
            }
            commentMap.put(Param.comment.praise, PRAISE_ALIAS.getOrDefault(praise, praise));
            commentList.add(commentMap);
        }
        page.putField(DataPushPipeline.DATA_LIST, commentList);

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
