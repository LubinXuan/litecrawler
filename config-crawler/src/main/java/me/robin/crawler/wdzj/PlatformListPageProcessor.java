package me.robin.crawler.wdzj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.robin.crawler.BaseMatchPageProcessor;
import me.robin.crawler.Const;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformListPageProcessor extends BaseMatchPageProcessor {

    static Map<Integer, String> statusMap = new HashMap<>();

    static {
        statusMap.put(1, "正常");
        statusMap.put(2, "歇业");
        statusMap.put(3, "提现困难");
        statusMap.put(5, "跑路");
    }

    public PlatformListPageProcessor() {
        super("http://www.wdzj.com/front_select-plat?sort=grade&currPage=");
    }

    @Override
    public MatchOther processPage(Page page) {

        JSONObject jsonObject = JSON.parseObject(page.getRawText());
        int currentPage = jsonObject.getIntValue("currentPage");
        int totalPage = jsonObject.getIntValue("totalPage");
        if (totalPage > currentPage) {
            page.addTargetRequest(urlBase + (currentPage + 1));
        }

        JSONArray list = jsonObject.getJSONArray("list");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> extra = new HashMap<>();
            JSONObject plat = list.getJSONObject(i);
            extra.put(Const.platformName, plat.getString("platName"));
            extra.put(Const.cityName, plat.getString("cityName"));
            extra.put(Const.platEarnings, plat.getFloatValue("platEarnings"));
            extra.put(Const.underlyingAssetType, plat.getString("term"));
            extra.put(Const.compositeIndex, plat.getIntValue("zonghezhishu"));
            extra.put(Const.platformStatus, plat.getIntValue("platStatus"));
            String platId = plat.getString("platId");
            Request request = new Request(PlatformDetailProcessor.url + platId);
            request.setExtras(extra);
            request.addHeader("referer", page.getRequest().getUrl());
            page.addTargetRequest(request);
        }


        return MatchOther.NO;
    }
}
