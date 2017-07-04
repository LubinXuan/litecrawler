package me.robin.crawler;

/**
 * Created by LubinXuan on 2017/7/4.
 */
public interface Param {

    String source = "source";

    interface comment {
        String platname = "platname";
        String productname = "productname";
        String remark = "remark";
        String remarktime = "remarktime";
        String username = "username";
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
