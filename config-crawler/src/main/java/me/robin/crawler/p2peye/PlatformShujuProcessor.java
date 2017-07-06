package me.robin.crawler.p2peye;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.util.TypeUtils;
import me.robin.crawler.Param;
import me.robin.crawler.common.CralwData;
import me.robin.crawler.common.RegexProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformShujuProcessor extends RegexProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PlatformShujuProcessor.class);

    public PlatformShujuProcessor() {
        super("http://(.*?).p2peye.com/shuju/.*?");
    }

    @Override
    public MatchOther processPage(Page page) {
        try {
            Object data = JSONPath.read(page.getRawText(), "data.data[0]");
            if (data instanceof JSONObject) {
                page.putField(Param.plat.stage, JSONPath.eval(data, "loan_period.value"));
                page.putField(Param.plat.totaldeal, JSONPath.eval(data, "amount.value"));
                page.putField(Param.plat.totaluser, JSONPath.eval(data, "invest_num.value"));
                page.putField(Param.plat.yield, TypeUtils.castToFloat(JSONPath.eval(data, "rate.value")) * 100);
            } else {
                logger.info("没有获取到有效数据:{}", page.getRequest().getUrl());
            }
            CralwData.platData(page.getResultItems());
            page.getRequest().getExtras().forEach(page::putField);
        } catch (Exception e) {
            page.getResultItems().setSkip(true);
            logger.warn("数据处理异常:{}", page.getRequest().getUrl());
        }
        return MatchOther.NO;
    }
}
