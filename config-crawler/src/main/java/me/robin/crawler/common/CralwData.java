package me.robin.crawler.common;

import me.robin.crawler.crawlers.Param;
import us.codecraft.webmagic.ResultItems;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lubin.Xuan on 2017-07-06.
 * {desc}
 */
public class CralwData {
    public static Map<String, Object> commentData() {
        Map<String, Object> data = new HashMap<>();
        data.put(Param.dataType, Param.comment.class.getSimpleName());
        return data;
    }

    public static Map<String, Object> platData() {
        Map<String, Object> data = new HashMap<>();
        data.put(Param.dataType, Param.plat.class.getSimpleName());
        return data;
    }


    public static void platData(ResultItems resultItems) {
        resultItems.put(Param.dataType, Param.plat.class.getSimpleName());
    }

    public static Map<String, Object> productData() {
        Map<String, Object> data = new HashMap<>();
        data.put(Param.dataType, Param.product.class.getSimpleName());
        return data;
    }


    public static void productData(ResultItems resultItems) {
        resultItems.put(Param.dataType, Param.product.class.getSimpleName());
    }
}
