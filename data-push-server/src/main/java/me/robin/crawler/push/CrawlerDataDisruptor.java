package me.robin.crawler.push;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.Executors;

/**
 * Created by LubinXuan on 2017/7/5.
 * lmax.disruptor 数据提交
 */
@Component
public class CrawlerDataDisruptor {

    private static final ThreadLocal<Cipher> CIPHER_LOCAL = new ThreadLocal<>();

    @Value("${crawler.server}")
    private String server;

    @Value("${crawler.rsa}")
    private String rsaPriKey;

    private MediaType APPLICATION_JSON = MediaType.parse("application/json");

    private Disruptor<CrawlerDataEvent> disruptor;

    private PrivateKey privateKey;


    private CrawlerDataDisruptor() {
        EventFactory<CrawlerDataEvent> eventFactory = new CrawlerDataEventFactory();
        int ringBufferSize = (int) Math.pow(2, 17); // RingBuffer 大小，必须是 2 的 N 次方；
        disruptor = new Disruptor<>(eventFactory,
                ringBufferSize, Executors.defaultThreadFactory(), ProducerType.MULTI,
                new YieldingWaitStrategy());
        EventHandler eventHandler = new CrawlerDataEventHandler();
        disruptor.handleEventsWith(eventHandler);
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
    }

    private class CrawlerDataEventFactory implements EventFactory<CrawlerDataEvent> {
        @Override
        public CrawlerDataEvent newInstance() {
            return new CrawlerDataEvent();
        }
    }


    private class CrawlerDataEventHandler implements EventHandler<CrawlerDataEvent> {

        OkHttpClient client = new OkHttpClient();

        @Override
        public void onEvent(CrawlerDataEvent crawlerDataEvent, long l, boolean b) throws Exception {

            if (null == crawlerDataEvent.data) {
                return;
            }
            try {
                switch (crawlerDataEvent.dataType) {
                    case "comment":
                    case "plat":
                    case "product":
                        break;
                    default:
                        return;
                }

                String api = "/financial-web/api/v1/spider/" + crawlerDataEvent.dataType + "/add";
                //todo upload data;
                System.out.println(crawlerDataEvent.data.toJSONString());
                if (!crawlerDataEvent.data.containsKey("sign")) {
                    signParam(crawlerDataEvent);
                }
                Request.Builder requestBuilder = new Request.Builder();
                requestBuilder.url("http://" + server + api);
                RequestBody requestBody = RequestBody.create(APPLICATION_JSON, crawlerDataEvent.data.toJSONString());
                Request request = requestBuilder.post(requestBody).build();
                //client.newCall(request).execute();
            } finally {
                crawlerDataEvent.setData(null);
                crawlerDataEvent.setDataType(null);
            }
        }
    }

    private void signParam(CrawlerDataEvent crawlerDataEvent) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        StringBuilder builder = new StringBuilder();
        for (String key : crawlerDataEvent.data.keySet()) {
            String value = crawlerDataEvent.data.getString(key);
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(key).append("=").append(value);
        }
        byte[] signData = cipher().doFinal(builder.toString().getBytes(Charset.forName("utf-8")));
        crawlerDataEvent.data.put("sign", new String(signData, Charset.forName("utf-8")));
    }

    public void pushData(String dataType, JSONObject jsonData) {
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
}
