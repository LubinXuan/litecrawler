package me.robin.crawler.crawlers.p2peye;

import me.robin.crawler.crawlers.Param;
import me.robin.crawler.common.BaseMatchPageProcessor;
import me.robin.crawler.common.RegexProcessor;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.handler.CompositePageProcessor;
import us.codecraft.webmagic.selector.HtmlNode;
import us.codecraft.webmagic.selector.Selectable;

/**
 * Created by Lubin.Xuan on 2017-07-06.
 * {desc}
 */
public class ProductProcessors {
    /**
     * Created by Lubin.Xuan on 2017-07-06.
     * {desc}
     */
    public static class ProductDetailProcessor extends RegexProcessor {

        public ProductDetailProcessor() {
            super("http://licai.p2peye.com/loans/details-(\\d+?).html");
        }

        @Override
        public MatchOther processPage(Page page) {
            String platName = page.getHtml().$("div.sub div.hd a", "title").get();
            String productName = page.getHtml().$("div.bigbox div.hd h3", "title").get();
            String repaymentType = page.getHtml().$("tr:contains(还款方式) td.bcell", "allText").all().get(1);
            String assetsType = page.getHtml().$("tr:contains(项目类型) td.mcell", "allText").get();
            String yield = StringUtils.replace(page.getHtml().$("div.des-le", "allText").get(), "%", "");
            String stage = StringUtils.replace(page.getHtml().$("div.dt2 div.bot", "allText").get(), "%", "");
            if (StringUtils.contains(stage, "个月")) {
                stage = StringUtils.remove(stage, "个月");
                stage = Integer.toString((int) Float.parseFloat(stage) * 30);
            } else if (StringUtils.contains(stage, "天")) {
                stage = StringUtils.remove(stage, "天");
            }

            String instruction = page.getHtml().$("div.pdetails", "allText").get();
            instruction = StringUtils.substringBefore(instruction, "平台信息");

            page.putField(Param.product.assetstype, assetsType);
            page.putField(Param.product.platName, platName);
            page.putField(Param.product.name, productName);
            page.putField(Param.product.yield, yield);
            page.putField(Param.product.repaymenttype, repaymentType);
            page.putField(Param.product.stage, stage);
            page.putField(Param.product.instruction, instruction);
            page.putField(Param.dataUid, StringUtils.substringBetween(page.getRequest().getUrl(), "details-", ".html"));
            page.putField(Param.dataType, Param.product.class.getSimpleName());
            return MatchOther.NO;
        }
    }

    /**
     * Created by Lubin.Xuan on 2017-07-06.
     * {desc}
     */
    public static class ProductListProcessor extends BaseMatchPageProcessor {
        public ProductListProcessor() {
            super("http://licai.p2peye.com/loans");
        }

        @Override
        public MatchOther processPage(Page page) {
            HtmlNode products = (HtmlNode) page.getHtml().$("a.ibds-a");
            for (Selectable product : products.nodes()) {
                Request request = new Request(product.$("a", "href").get());
                request.setPriority(1);
                page.addTargetRequest(request);
            }
            String nextUrl = page.getHtml().$("a[title=下一页]", "abs:href").get();
            if (StringUtils.isNotBlank(nextUrl)) {
                Request request = new Request(nextUrl);
                request.setPriority(1);
                page.addTargetRequest(request);
            }
            return MatchOther.NO;
        }
    }
}
