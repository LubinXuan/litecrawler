package me.robin.crawler.common;

import me.robin.crawler.service.SpiderService;

/**
 * Created by Lubin.Xuan on 2017-07-06.
 * {desc}
 */
public class KVStoreClient {

    private static SpiderService service;

    public static void setService(SpiderService service) {
        KVStoreClient.service = service;
    }

    public static String get(String key) {

        if (null == service) {
            return null;
        }

        return service.getValue(key);
    }

    public static void set(String key, Object value) {

        if (null == service) {
            return;
        }

        service.setValue(key, String.valueOf(value));
    }
}
