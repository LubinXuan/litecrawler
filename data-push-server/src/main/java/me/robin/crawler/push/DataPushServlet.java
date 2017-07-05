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
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by LubinXuan on 2017/7/5.
 */
@WebServlet(name = "推送数据接收网关", value = "/push/data")
public class DataPushServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DataPushServlet.class);

    private static final ThreadLocal<NumberFormat> nf = ThreadLocal.withInitial(() -> new DecimalFormat("00000000"));

    private static final Map<String, AtomicLong> id = new ConcurrentHashMap<>();

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    private String dateKey = sdf.format(Calendar.getInstance().getTime());

    @Resource
    private CrawlerDataDisruptor crawlerDataDisruptor;

    //每天0点0分清理计数器
    @Scheduled(cron = "0 0 0 */1 * ?")
    private void clear() throws InterruptedException {
        Set<String> keys = new HashSet<>(id.keySet());
        dateKey = sdf.format(Calendar.getInstance().getTime());

        TimeUnit.MINUTES.sleep(1);

        for (String key : keys) {
            String dt = StringUtils.substringBetween(key, "-");
            if (dateKey.compareTo(dt) > 0) {
                id.remove(key);
                logger.info("计数器失效,移除计数器: {}", key);
            }
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String data = IOUtils.toString(req.getInputStream(), Charset.forName("utf-8"));
        String dataType = req.getHeader("data-type");
        String key = req.getHeader("source-type");
        String prefix = key + "-" + dateKey + "-";
        AtomicLong atomicLong = id.computeIfAbsent(prefix, s -> new AtomicLong(0));
        JSONObject jsonData = JSON.parseObject(data);
        String serialno = prefix + nf.get().format(atomicLong.incrementAndGet());
        jsonData.put("serialno", serialno);
        crawlerDataDisruptor.pushData(dataType, jsonData);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("ok remainingCapacity:" + crawlerDataDisruptor.remainingCapacity());
    }
}
