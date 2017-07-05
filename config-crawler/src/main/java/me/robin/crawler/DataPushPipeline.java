package me.robin.crawler;

import com.alibaba.fastjson.JSON;
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Lubin.Xuan on 2017-07-05.
 */
public class DataPushPipeline implements Pipeline {

    private static final Logger logger = LoggerFactory.getLogger(DataPushPipeline.class);

    private static final HttpClient client = HttpClients.createDefault();

    private HttpHost httpHost = new HttpHost("127.0.0.1", 8080);

    public DataPushPipeline() {
    }

    public DataPushPipeline(String host, int port) {
        this.httpHost = new HttpHost(host, port);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> data = resultItems.getAll();
        String dataType = (String) data.remove(Param.dataType);
        this.push(dataType, data);
    }

    private void push(String dataType, Map<String, Object> data) {
        HttpPost post = new HttpPost("/push/data");
        post.setEntity(new StringEntity(JSON.toJSONString(data), Charset.forName("utf-8")));
        post.setHeader("data-type", dataType);
        HttpResponse response = null;
        int maxTry = 10;
        Throwable throwable = null;
        int statusCode = 0;
        while (maxTry > 0) {
            try {
                response = client.execute(httpHost, post);
                if (response.getStatusLine().getStatusCode() == 200) {
                    return;
                }
                statusCode = response.getStatusLine().getStatusCode();
            } catch (IOException e) {
                throwable = e;
            } finally {
                if (null != response) {
                    EntityUtils.consumeQuietly(response.getEntity());
                }
                post.releaseConnection();
            }
            maxTry--;
            try {
                TimeUnit.SECONDS.sleep(10 - maxTry);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (null != throwable) {
            logger.warn("数据上传异常,Exception:{}", throwable.getMessage());
        } else {
            logger.warn("数据上传返回码异常:{}", statusCode);
        }
    }
}
