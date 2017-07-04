package me.robin.crawler.wdzj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import me.robin.crawler.BaseMatchPageProcessor;
import me.robin.crawler.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformDetailProcessor extends BaseMatchPageProcessor {

    private static final JSONObject EMPTY = new JSONObject();

    private static final Logger logger = LoggerFactory.getLogger(PlatformDetailProcessor.class);

    public static final String url = "http://wwwservice.wdzj.com/api/plat/platData30Days?platId=";

    public PlatformDetailProcessor() {
        super(url);
    }

    @Override
    public MatchOther processPage(Page page) {
        JSONObject data = JSON.parseObject(page.getRawText());
        boolean status = data.getBooleanValue("success");
        if (status) {
            JSONObject platShujuMap = (JSONObject) JSONPath.eval(data, "data.platShujuMap");
            JSONObject platOuterVo = (JSONObject) JSONPath.eval(data, "data.platOuterVo");
            page.getRequest().getExtras().put(Param.plat.homelink, platOuterVo.getString("platUrl"));
            page.getRequest().getExtras().put(Param.plat.logo, platOuterVo.getString("platLogoUrl"));
            if (null == platShujuMap) {
                platShujuMap = EMPTY;
            }
            page.getRequest().getExtras().put(Param.plat.totaluser, platShujuMap.getIntValue("bidder_num"));
            page.getRequest().getExtras().put(Param.plat.totaldeal, platShujuMap.getFloatValue("amount"));
            page.getRequest().getExtras().put(Param.plat.stage, platShujuMap.getFloatValue("loan_period"));


            Request request = new Request(PlatformDetailHtmlProcessor.url + platOuterVo.getString("platNamePin") + "/");
            request.setExtras(page.getRequest().getExtras());
            request.addHeader("referer", page.getRequest().getHeaders().get("referer"));
            page.addTargetRequest(request);
        } else {
            logger.warn("网贷之家详情数据获取异常:{} {}", page.getRequest().getUrl(), data.getString("message"));
        }

        return MatchOther.NO;
    }
}
