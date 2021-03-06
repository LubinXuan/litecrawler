package me.robin.crawler.common;

import com.alibaba.fastjson.JSON;
import me.robin.crawler.crawlers.Param;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Lubin.Xuan on 2017-07-05.
 */
public class DataPushPipeline implements Pipeline {

    private static final Logger logger = LoggerFactory.getLogger(DataPushPipeline.class);

    private static final HttpClient client = HttpClients.createDefault();

    public static final String DATA_LIST = "DATA_LIST";

    private static HttpHost httpHost = new HttpHost("127.0.0.1", 8080);

    private final Param.PlatName platName;

    private final Map<String, Set<String>> fieldMap = new HashMap<>();

    private final AtomicBoolean serverLock = new AtomicBoolean(false);

    public DataPushPipeline(Param.PlatName platName) {
        this.platName = platName;
        initField(Param.plat.class);
        initField(Param.comment.class);
        initField(Param.product.class);
    }

    private void initField(Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Set<String> allFields = new HashSet<>();
        for (Field field : fields) {
            try {
                String value = (String) field.get(null);
                allFields.add(value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (!allFields.isEmpty()) {
            fieldMap.put(clazz.getSimpleName(), allFields);
        }
    }

    public static void host(String host, int port) {
        DataPushPipeline.httpHost = new HttpHost(host, port);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        List<Map<String, Object>> dataList = resultItems.get(DATA_LIST);
        if (null != dataList) {
            Boolean update = resultItems.get(Param.cursor_limit_update);
            if (Boolean.TRUE.equals(update)) {
                Object value = resultItems.get(Param.cursor_limit_save);
                String key = resultItems.get(Param.cursor_limit_key);
                KVStoreClient.set(platName.name(), key, value);
            }
            for (Map<String, Object> data : dataList) {
                String dataType = (String) data.remove(Param.dataType);
                this.push(dataType, data);
            }
        } else {
            Map<String, Object> data = resultItems.getAll();
            String dataType = (String) data.remove(Param.dataType);
            this.push(dataType, data);
        }
    }

    //数据提交死循环至完成提交服务器
    private void push(String dataType, Map<String, Object> data) {

        if (data.isEmpty()) {
            return;
        }

        Set<String> allFields = fieldMap.get(dataType);
        for (String field : allFields) {
            Object value = data.get(field);
            if (value instanceof String) {
                data.put(field, StringUtils.trim((String) value));
            }
        }

        if (!data.containsKey(Param.source)) {
            data.put(Param.source, platName.getName());
        }


        WaitUtil.waitObject(serverLock);

        HttpPost post = new HttpPost("/push/data");
        post.setEntity(new StringEntity(JSON.toJSONString(data), Charset.forName("utf-8")));
        post.setHeader("data-type", dataType);
        post.setHeader("source-type", platName.name());
        HttpResponse response = null;
        Throwable throwable = null;
        int statusCode = 0;
        int log = 0;
        while (true) {
            try {
                response = client.execute(httpHost, post);
                if (response.getStatusLine().getStatusCode() == 200) {
                    serverLock.set(false);
                    synchronized (serverLock) {
                        serverLock.notifyAll();
                    }
                    return;
                }
                serverLock.set(true);
                statusCode = response.getStatusLine().getStatusCode();
            } catch (Exception e) {
                throwable = e;
            } finally {
                if (null != response) {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
                post.releaseConnection();
            }
            if (log++ % 5 == 0) {
                if (null != throwable) {
                    logger.warn("数据上传异常,Exception:{}", throwable.getMessage());
                } else {
                    logger.warn("数据上传返回码异常:{}", statusCode);
                }
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
