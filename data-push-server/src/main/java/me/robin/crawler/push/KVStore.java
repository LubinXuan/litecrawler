package me.robin.crawler.push;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lubin.Xuan on 2017-07-06.
 */
@Component
public class KVStore implements DisposableBean {

    @Value("${kv_store.fileName}")
    private File storeFile;

    private ConcurrentHashMap<String, String> data;

    @PostConstruct
    private void init() throws IOException {
        data = SerializationUtil.fromFile(storeFile, ConcurrentHashMap::new);
    }

    public void set(String key, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        this.data.put(key, value);
    }

    public String get(String key) {
        return this.data.get(key);
    }

    @Override
    public void destroy() throws Exception {
        SerializationUtil.save(storeFile, data);
    }
}
