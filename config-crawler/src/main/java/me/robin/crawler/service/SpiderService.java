package me.robin.crawler.service;

import me.robin.crawler.common.KVStoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by Lubin.Xuan on 2017-07-11.
 * {desc}
 */
@Service
public class SpiderService {

    private static final Logger logger = LoggerFactory.getLogger(SpiderService.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    private void init() {
        KVStoreClient.setService(this);
    }

    public String getValue(String type, String key) {
        try {
            return this.stringRedisTemplate.<String, String>opsForHash().get(type, key);
        } catch (Exception e) {
            logger.warn("缓存读取异常", e);
            return null;
        }
    }

    public void setValue(String type, String key, String value) {
        try {
            this.stringRedisTemplate.<String, String>opsForHash().put(type, key, value);
        } catch (Exception e) {
            logger.warn("缓存保存异常", e);
        }
    }
}
