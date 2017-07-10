package me.robin.crawler.wdzj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import me.robin.crawler.common.BaseMatchPageProcessor;
import me.robin.crawler.Param;
import org.apache.commons.lang3.StringUtils;
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
            page.getRequest().putExtra(Param.plat.homelink, platOuterVo.getString("platUrl"));
            page.getRequest().putExtra(Param.plat.logo, platOuterVo.getString("platLogoUrl"));
            if (null == platShujuMap) {
                platShujuMap = EMPTY;
            }
            page.getRequest().putExtra(Param.plat.totaluser, platShujuMap.getIntValue("bidder_num"));
            page.getRequest().putExtra(Param.plat.totaldeal, platShujuMap.getFloatValue("amount"));
            page.getRequest().putExtra(Param.plat.stage, platShujuMap.getFloatValue("loan_period"));

            String locationAreaName = platOuterVo.getString("locationAreaName");
            String locationCityName = platOuterVo.getString("locationCityName");

            if (StringUtils.isNotBlank(locationAreaName) && StringUtils.isNotBlank(locationCityName)) {
                page.getRequest().putExtra(Param.plat.location, locationAreaName + locationCityName);
            } else if (StringUtils.isNotBlank(locationAreaName) && StringUtils.isBlank(locationCityName)) {
                page.getRequest().putExtra(Param.plat.location, locationAreaName);
            } else if (StringUtils.isBlank(locationAreaName) && StringUtils.isNotBlank(locationCityName)) {
                page.getRequest().putExtra(Param.plat.location, locationCityName);
            }

            Request request = new Request(PlatformDetailHtmlProcessor.url + platOuterVo.getString("platNamePin") + "/");
            request.setExtras(page.getRequest().getExtras());
            request.addHeader("referer", page.getRequest().getHeaders().get("referer"));
            request.setPriority(1);
            page.addTargetRequest(request);
        } else {
            logger.warn("网贷之家详情数据获取异常:{} {}", page.getRequest().getUrl(), data.getString("message"));
        }
        page.getResultItems().setSkip(true);
        return MatchOther.NO;
    }
}
