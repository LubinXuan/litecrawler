package me.robin.crawler.push;

import com.alibaba.fastjson.JSONObject;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by LubinXuan on 2017/7/5.
 * lmax.disruptor 数据提交
 */
@Component
public class CrawlerDataDisruptor {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerDataDisruptor.class);

    private static final ThreadLocal<Cipher> CIPHER_LOCAL = new ThreadLocal<>();

    private static final OkHttpClient client = new OkHttpClient();

    @Value("${crawler.server}")
    private String server;

    @Value("${crawler.rsa}")
    private String rsaPriKey;

    private MediaType APPLICATION_JSON = MediaType.parse("application/json");

    private Disruptor<CrawlerDataEvent> disruptor;

    private PrivateKey privateKey;

    private ExecutorService service = Executors.newFixedThreadPool(1);

    private CrawlerDataDisruptor() {
        EventFactory<CrawlerDataEvent> eventFactory = new CrawlerDataEventFactory();
        int ringBufferSize = (int) Math.pow(2, 15); // RingBuffer 大小，必须是 2 的 N 次方；
        disruptor = new Disruptor<>(eventFactory,
                ringBufferSize, Executors.defaultThreadFactory(), ProducerType.SINGLE,
                new YieldingWaitStrategy());
        WorkHandler workHandler = new CrawlerDataHandler();
        disruptor.handleEventsWithWorkerPool(workHandler,workHandler,workHandler,workHandler);
    }

    @PostConstruct
    private void init() throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaPriKey));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKey = kf.generatePrivate(spec);
        disruptor.start();
    }

    public synchronized Cipher cipher() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        if (null == CIPHER_LOCAL.get()) {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            CIPHER_LOCAL.set(cipher);
        }
        return CIPHER_LOCAL.get();
    }

    class CrawlerDataEvent {
        private JSONObject data;

        private String dataType;

        void setData(JSONObject data) {
            this.data = data;
        }

        void setDataType(String dataType) {
            this.dataType = dataType;
        }

        void clean() {
            this.data = null;
            this.dataType = null;
        }
    }

    private class CrawlerDataEventFactory implements EventFactory<CrawlerDataEvent> {
        @Override
        public CrawlerDataEvent newInstance() {
            return new CrawlerDataEvent();
        }
    }


    private class CrawlerDataHandler implements EventHandler<CrawlerDataEvent>, WorkHandler<CrawlerDataEvent> {
        @Override
        public void onEvent(CrawlerDataEvent event) throws Exception {
            if (null == event.data) {
                return;
            }
            try {
                switch (event.dataType) {
                    case "comment":
                    case "plat":
                    case "product":
                        break;
                    default:
                        return;
                }

                String api = "/financial-web/api/v1/spider/" + event.dataType + "/add";
                //todo upload data;
                if (!event.data.containsKey("sign")) {
                    signParam(event);
                }
                Request.Builder requestBuilder = new Request.Builder();
                requestBuilder.url("http://" + server + api);
                RequestBody requestBody = RequestBody.create(APPLICATION_JSON, event.data.toJSONString());
                Request request = requestBuilder.post(requestBody).build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    String rsp = null != response.body() ? response.body().string() : "";
                    if (response.code() != 200 || StringUtils.contains(rsp, "验签失败")) {
                        logger.warn("数据提交服务器响应异常:{}  data:{}  rsp:{}", response.code(), event.data.toJSONString(), rsp);
                        pushData(event.dataType, event.data);
                    }
                } catch (IOException e) {
                    logger.warn("数据提交服务器异常");
                    pushData(event.dataType, event.data);
                } finally {
                    IOUtils.closeQuietly(response);
                }
            } finally {
                event.clean();
            }
        }

        @Override
        public void onEvent(CrawlerDataEvent event, long l, boolean b) throws Exception {
            this.onEvent(event);
        }
    }

    private void signParam(CrawlerDataEvent crawlerDataEvent) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        StringBuilder builder = new StringBuilder();
        for (String key : new TreeSet<>(crawlerDataEvent.data.keySet())) {
            String value = crawlerDataEvent.data.getString(key);
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(key).append("=").append(value);
        }
        byte[] signData = cipher().doFinal(DigestUtils.sha1(builder.toString()));
        crawlerDataEvent.data.put("sign", new String(Base64.getEncoder().encode(signData), Charset.forName("utf-8")));
    }

    public void pushData(String dataType, JSONObject jsonData) {
        service.execute(() -> _pushData(dataType, jsonData));
    }

    private void _pushData(String dataType, JSONObject jsonData) {
        RingBuffer<CrawlerDataEvent> ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        try {
            CrawlerDataEvent event = ringBuffer.get(sequence);
            event.setData(jsonData);
            event.setDataType(dataType);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    public long remainingCapacity() {
        return disruptor.getRingBuffer().remainingCapacity();
    }
}
