package me.robin.crawler.push;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by LubinXuan on 2017/7/5.
 */
@WebServlet(name = "推送数据接收网关", value = "/push/data")
public class DataPushServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String data = IOUtils.toString(req.getInputStream(), Charset.forName("utf-8"));
        String dataType = req.getHeader("data-type");
        CrawlerDataDisruptor.ins.pushData(dataType, data);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("ok");
    }
}
