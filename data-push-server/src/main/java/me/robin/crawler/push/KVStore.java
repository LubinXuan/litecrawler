package me.robin.crawler.push;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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

    private Map<String, String> data = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() throws IOException {
        if (storeFile.exists()) {
            List<String> lines = FileUtils.readLines(storeFile, Charset.forName("utf-8"));
            for (String line : lines) {
                String[] kv = StringUtils.split(line, ":");
                data.put(kv[0], kv[1]);
            }
        }
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
        FileOutputStream fileOutputStream = FileUtils.openOutputStream(storeFile);
        BufferedOutputStream bos = IOUtils.buffer(fileOutputStream);
        int lines = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String line = entry.getKey() + ":" + entry.getValue();
            bos.write(line.getBytes(Charset.forName("utf-8")));
            if (lines++ > 10000) {
                bos.flush();
            }
        }
        bos.close();
        IOUtils.closeQuietly(fileOutputStream);
    }
}
