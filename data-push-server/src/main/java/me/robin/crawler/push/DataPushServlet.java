package me.robin.crawler.push;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by LubinXuan on 2017/7/5.
 */
@WebServlet(name = "推送数据接收网关", value = "/push/data")
public class DataPushServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DataPushServlet.class);

    private static final ThreadLocal<NumberFormat> nf = ThreadLocal.withInitial(() -> new DecimalFormat("00000000"));

    private static final File serializableFile = new File("./count.dat");

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    private String dateKey = sdf.format(Calendar.getInstance().getTime());

    private ConcurrentHashMap<String, AtomicInteger> idCache = SerializationUtil.fromFile(serializableFile, ConcurrentHashMap::new);

    @Resource
    private CrawlerDataDisruptor crawlerDataDisruptor;

    //每天0点0分清理计数器
    @Scheduled(cron = "0 0 0 */1 * ?")
    private void clear() throws InterruptedException {
        Set<String> keys = new HashSet<>(idCache.keySet());
        dateKey = sdf.format(Calendar.getInstance().getTime());

        TimeUnit.MINUTES.sleep(1);

        for (String key : keys) {
            String dt = StringUtils.substringBetween(key, "-");
            if (dateKey.compareTo(dt) > 0) {
                idCache.remove(key);
                logger.info("计数器失效,移除计数器: {}", key);
            }
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String data = IOUtils.toString(req.getInputStream(), Charset.forName("utf-8"));
        String dataType = req.getHeader("data-type");
        String key = req.getHeader("source-type");
        JSONObject jsonData = JSON.parseObject(data);
        String uid = jsonData.getString("uid");
        if (StringUtils.isNotBlank(uid)) {
            jsonData.put("serialno", key + "-" + uid);
            jsonData.remove("uid");
        } else {
            String prefix = key + "-" + dateKey + "-";
            AtomicInteger increment = idCache.computeIfAbsent(prefix, s -> new AtomicInteger(0));
            String serialNo = prefix + nf.get().format(increment.incrementAndGet());
            jsonData.put("serialno", serialNo);
        }
        crawlerDataDisruptor.pushData(dataType, jsonData);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("ok remainingCapacity:" + crawlerDataDisruptor.remainingCapacity());
    }

    @Override
    public void destroy() {
        logger.info("停止数据接收网关,保存序列号生成器");
        try {
            SerializationUtil.save(serializableFile, idCache);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.destroy();
    }
}
