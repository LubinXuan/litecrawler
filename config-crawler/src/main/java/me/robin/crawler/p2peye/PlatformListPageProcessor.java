package me.robin.crawler.p2peye;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import me.robin.crawler.Param;
import me.robin.crawler.common.BaseMatchPageProcessor;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformListPageProcessor extends BaseMatchPageProcessor {

    public PlatformListPageProcessor() {
        super("http://lu.p2peye.com/index/getPlatform");
    }

    @Override
    public MatchOther processPage(Page page) {
        JSONArray platforms = (JSONArray) JSONPath.read(page.getRawText(), "data");
        for (int i = 0; i < platforms.size(); i++) {
            JSONObject platform = platforms.getJSONObject(i);
            Request request = new Request("http://" + platform.getString("domain_body") + ".p2peye.com");
            String platName = platform.getString("name");
            request.putExtra(Param.plat.name,platName);
            page.addTargetRequest(request);
            request = new Request("http://" + platform.getString("domain_body") + ".p2peye.com/comment/");
            request.putExtra(Param.comment.platname,platName);
            page.addTargetRequest(request);
        }
        page.getResultItems().setSkip(true);
        return MatchOther.NO;
    }
}
