package me.robin.crawler.crawlers.wdzj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.robin.crawler.common.KVStoreClient;
import me.robin.crawler.crawlers.Param;
import me.robin.crawler.common.RegexProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformListPageProcessor extends RegexProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PlatformListPageProcessor.class);

    static Map<Integer, String> statusMap = new HashMap<>();

    static {
        statusMap.put(1, "正常");
        statusMap.put(2, "歇业");
        statusMap.put(3, "提现困难");
        statusMap.put(5, "跑路");
    }

    public PlatformListPageProcessor() {
        super("http://www.wdzj.com/front_select-plat\\?sort=(.*?)&currPage=(.*?)");
    }

    @Override
    public MatchOther processPage(Page page) {

        JSONObject jsonObject = JSON.parseObject(page.getRawText());
        int currentPage = jsonObject.getIntValue("currentPage");
        int totalPage = jsonObject.getIntValue("totalPage");
        if (totalPage > currentPage) {
            page.addTargetRequest(page.getRequest().getUrl().replace("=" + currentPage, "=" + (currentPage + 1)));
        } else {
            logger.info("网贷之家平台列表爬取完成");
        }

        JSONArray list = jsonObject.getJSONArray("list");
        for (int i = 0; i < list.size(); i++) {
            JSONObject plat = list.getJSONObject(i);
            String platId = plat.getString("platId");
            Request request = new Request(PlatformDetailProcessor.url + platId);
            String platName = plat.getString("platName");
            request.putExtra(Param.plat.name, platName);
            request.putExtra(Param.plat.onlinetime, plat.getString("onlineDate"));
            request.putExtra(Param.plat.rank, plat.getIntValue("zonghezhishuRank"));
            request.putExtra(Param.plat.score, plat.getFloatValue("zonghezhishu"));
            request.putExtra(Param.plat.yield, plat.getFloatValue("platEarnings"));
            request.putExtra(Param.dataUid, platId);
            //int platStatus = plat.getIntValue("platStatus");
            request.addHeader("referer", page.getRequest().getUrl());
            request.setPriority(1);
            page.addTargetRequest(request);
            //if (1 == platStatus || platStatus == 3) {
            request = new Request(CommentProcessor.commentUrl(platId, 1));
            request.setMethod(HttpConstant.Method.POST);
            request.putExtra(Param.comment.platname, platName);
            request.putExtra(Param.cursor_limit, KVStoreClient.get(Param.PlatName.WDZJ + "-" + platName));
            request.addHeader("referer", "http://www.wdzj.com/dangan/" + plat.getString("platNamePin") + "/dianping/");
            page.addTargetRequest(request);
            //}
        }
        page.getResultItems().setSkip(true);
        return MatchOther.NO;
    }
}
