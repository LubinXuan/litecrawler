package me.robin.crawler.wdzj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.robin.crawler.Param;
import me.robin.crawler.util.RegexProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

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
            Map<String, Object> extra = new HashMap<>();
            JSONObject plat = list.getJSONObject(i);
            extra.put(Param.plat.name, plat.getString("platName"));
            extra.put(Param.plat.onlinetime, plat.getString("onlineDate") + " 00:00:00");
            extra.put(Param.plat.rank, plat.getIntValue("zonghezhishuRank"));
            extra.put(Param.plat.score, plat.getFloatValue("zonghezhishu"));
            extra.put(Param.plat.yield, plat.getFloatValue("platEarnings"));
            String platId = plat.getString("platId");
            Request request = new Request(PlatformDetailProcessor.url + platId);
            request.setExtras(extra);
            request.addHeader("referer", page.getRequest().getUrl());
            page.addTargetRequest(request);
        }


        return MatchOther.NO;
    }
}
