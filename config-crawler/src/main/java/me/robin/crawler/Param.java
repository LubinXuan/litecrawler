package me.robin.crawler;

/**
 * Created by LubinXuan on 2017/7/4.
 */
public interface Param {

    interface PlatName {
        String wdzj = "网贷之家";
        String p2peye = "网贷天眼";
        String rong360 = "融360";
    }

    String comment_id_limit = "comment_id_limit";

    String source = "source";
    String dataType = "dataType";

    interface comment {
        String platname = "platname";
        String productname = "productname";
        String remark = "remark";
        String remarktime = "remarktime";
        String username = "username";
        String praise = "praise";
    }

    interface plat {
        String homelink = "homelink";
        String instruction = "instruction";
        String logo = "logo";
        String name = "name";
        String onlinetime = "onlinetime";
        String rank = "rank";
        String score = "score";
        String stage = "stage";
        String totaldeal = "totaldeal";
        String totaluser = "totaluser";
        String yield = "yield";
    }

    interface product {
        String assetstype = "assetstype";
        String instruction = "instruction";
        String name = "name";
        String platName = "platName";
        String rank = "rank";
        String repaymenttype = "repaymenttype";
        String score = "score";
        String stage = "stage";
        String yield = "yield";
    }
}
