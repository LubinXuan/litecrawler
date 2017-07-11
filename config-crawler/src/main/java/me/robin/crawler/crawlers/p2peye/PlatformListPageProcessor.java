package me.robin.crawler.crawlers.p2peye;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import me.robin.crawler.crawlers.Param;
import me.robin.crawler.common.BaseMatchPageProcessor;
import me.robin.crawler.common.RegexProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformListPageProcessor extends RegexProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PlatformListPageProcessor.class);


    public PlatformListPageProcessor() {
        super("http://(.*?).p2peye.com/index/getPlatform");
    }

    @Override
    public MatchOther processPage(Page page) {
        JSONArray platforms = (JSONArray) JSONPath.read(page.getRawText(), "data");
        for (int i = 0; i < platforms.size(); i++) {
            JSONObject platform = platforms.getJSONObject(i);
            String domain = platform.getString("domain_body");
            if (StringUtils.isBlank(domain)) {
                logger.warn("平台数据异常:{}", platform.toJSONString());
            } else {
                Request request = new Request("http://" + platform.getString("domain_body") + ".p2peye.com");
                String platName = platform.getString("name");
                request.putExtra(Param.plat.name, platName);
                page.addTargetRequest(request);
            }
        }
        page.getResultItems().setSkip(true);
        return MatchOther.NO;
    }
}
