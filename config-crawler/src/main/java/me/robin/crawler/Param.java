package me.robin.crawler;

/**
 * Created by LubinXuan on 2017/7/4.
 */
public interface Param {

    enum PlatName {
        WDZJ("网贷之家"), P2PEYE("网贷天眼"), RONG360("融360");
        private String name;

        PlatName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    String comment_crawled = "comment_crawled";

    String comment_id_limit = "comment_id_limit";

    String comment_id_limit_save = "comment_id_limit_save";

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
