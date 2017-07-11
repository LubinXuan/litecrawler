package me.robin.crawler.push.servlet;

import me.robin.crawler.push.KVStore;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Lubin.Xuan on 2017/7/5.
 * {desc}
 */
@WebServlet(name = "索引接口", value = "/cursor")
public class CursorServlet extends HttpServlet {

    @Resource
    private KVStore kvStore;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String key = req.getParameter("key");
        if (StringUtils.isBlank(key)) {
            resp.getWriter().write("");
        } else {
            String value = kvStore.get(key);
            resp.getWriter().write(StringUtils.isBlank(value) ? "" : value);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String key = req.getParameter("key");
        String value = req.getParameter("value");
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return;
        }
        kvStore.set(key, value);
    }
}
