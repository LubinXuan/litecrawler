package me.robin.crawler.push;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by LubinXuan on 2017/7/5.
 */
@WebServlet(name = "推送数据接收网关", value = "/push/data")
public class DataPushServlet extends HttpServlet {

    private static final ThreadLocal<Map<String, SimpleDateFormat>> local = ThreadLocal.withInitial(HashMap::new);

    private static final ThreadLocal<NumberFormat> nf = ThreadLocal.withInitial(() -> new DecimalFormat("00000000"));

    private static final Map<String, AtomicLong> id = new ConcurrentHashMap<>();

    @Resource
    private CrawlerDataDisruptor crawlerDataDisruptor;


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String data = IOUtils.toString(req.getInputStream(), Charset.forName("utf-8"));
        String dataType = req.getHeader("data-type");
        String key = req.getHeader("source-type");
        String prefix = local.get().computeIfAbsent(key, s -> new SimpleDateFormat("'" + s + "'-yyyyMMdd-")).format(Calendar.getInstance().getTime());
        AtomicLong atomicLong = id.computeIfAbsent(prefix, s -> new AtomicLong(0));
        JSONObject jsonData = JSON.parseObject(data);
        String serialno = prefix + nf.get().format(atomicLong.incrementAndGet());
        jsonData.put("serialno", serialno);
        crawlerDataDisruptor.pushData(dataType, jsonData);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("ok");
    }
}
