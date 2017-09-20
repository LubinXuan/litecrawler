package me.robin.crawler.common;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.SpiderListener;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.scheduler.RedisPriorityScheduler;

/**
 * Created by Lubin.Xuan on 2017-09-20.
 * {desc}
 */
public class RedisPrioritySchedulerExt extends RedisPriorityScheduler implements Pipeline {
    public RedisPrioritySchedulerExt(JedisPool pool) {
        super(pool);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        String key = getItemKey(task);
        String field = DigestUtils.sha1Hex(resultItems.getRequest().getUrl());
        try (Jedis jedis = pool.getResource()) {
            jedis.hdel(key.getBytes(), field.getBytes());
        }catch (Exception ignore){}
    }
}
