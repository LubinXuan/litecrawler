package me.robin.crawler.p2peye;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.util.TypeUtils;
import me.robin.crawler.Param;
import me.robin.crawler.common.RegexProcessor;
import us.codecraft.webmagic.Page;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformShujuProcessor extends RegexProcessor {

    public PlatformShujuProcessor() {
        super("http://(.*?).p2peye.com/shuju/.*?");
    }

    @Override
    public MatchOther processPage(Page page) {
        JSONObject data = (JSONObject) JSONPath.read(page.getRawText(), "data.data[0]");
        page.putField(Param.plat.stage, JSONPath.eval(data, "loan_period.value"));
        page.putField(Param.plat.totaldeal, JSONPath.eval(data, "amount.value"));
        page.putField(Param.plat.totaluser, JSONPath.eval(data, "invest_num.value"));
        page.putField(Param.plat.yield, TypeUtils.castToFloat(JSONPath.eval(data, "rate.value")) * 100);
        page.putField(Param.dataType, Param.plat.class.getSimpleName());
        page.getRequest().getExtras().forEach(page::putField);
        return MatchOther.NO;
    }
}
