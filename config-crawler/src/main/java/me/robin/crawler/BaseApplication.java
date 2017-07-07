package me.robin.crawler;

import me.robin.crawler.common.DataPushPipeline;
import me.robin.crawler.common.KVStoreClient;

/**
 * Created by LubinXuan on 2017/6/3.
 */
abstract class BaseApplication {
    static {
        KVStoreClient.host("127.0.0.1", 8080);
        DataPushPipeline.host("127.0.0.1", 8080);
    }
}
