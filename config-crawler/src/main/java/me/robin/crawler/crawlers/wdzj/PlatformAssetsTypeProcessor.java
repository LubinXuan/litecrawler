package me.robin.crawler.crawlers.wdzj;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONPath;
import me.robin.crawler.crawlers.Param;
import me.robin.crawler.common.BaseMatchPageProcessor;
import me.robin.crawler.common.CralwData;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;

/**
 * Created by LubinXuan on 2017/6/3.
 */
public class PlatformAssetsTypeProcessor extends BaseMatchPageProcessor {

    public static final String url = "http://shuju.wdzj.com/basic-surface-";

    public PlatformAssetsTypeProcessor() {
        super(url);
    }

    @Override
    public MatchOther processPage(Page page) {
        Object value = JSONPath.read(page.getRawText(), "pie1.key");
        if (value instanceof JSONArray) {
            page.putField(Param.plat.projecttype, StringUtils.join((JSONArray) value, "|"));
        }
        page.getRequest().getExtras().forEach(page::putField);
        CralwData.platData(page.getResultItems());
        return MatchOther.NO;
    }
}
