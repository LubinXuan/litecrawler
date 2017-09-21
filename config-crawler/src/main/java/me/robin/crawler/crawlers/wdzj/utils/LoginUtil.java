package me.robin.crawler.crawlers.wdzj.utils;

import com.alibaba.fastjson.JSONPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created by Lubin.Xuan on 2017-09-20.
 * {desc}
 */
@Slf4j
public class LoginUtil {

    private static final HttpClient client;

    private static final CookieStore cookieStore = new BasicCookieStore();

    static {
        HttpClientBuilder builder = HttpClients.custom();
        builder.setDefaultCookieStore(cookieStore);
        client = builder.build();
    }

    public static List<Cookie> login(String username, String password) {
        HttpGet get = new HttpGet("https://passport.wdzj.com/userInterface/login?t=" + System.currentTimeMillis() + "&username=" + username + "&password=" + password + "&auto_login=0");
        HttpResponse response = null;
        try {
            get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36");
            response = client.execute(get);
            String content = EntityUtils.toString(response.getEntity());
            String msg = (String) JSONPath.read(content, "msg");
            if (StringUtils.equals("登录成功", msg)) {
                if (frontLogin()) {
                    return cookieStore.getCookies();
                }
            } else {
                log.warn("网贷之家登录失败:{}", content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            get.releaseConnection();
            if (null != response)
                EntityUtils.consumeQuietly(response.getEntity());
        }
        return null;
    }

    private static boolean frontLogin() {
        HttpGet get = new HttpGet("http://www.wdzj.com/front/login?callback=a");
        HttpResponse response = null;
        try {
            get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36");
            response = client.execute(get);
            String content = EntityUtils.toString(response.getEntity());
            String data = StringUtils.substringBetween(content, "(", ")");
            boolean success = !StringUtils.equalsIgnoreCase("-1", data);
            if (!success) {
                log.warn("网贷之家前端登录失败：{}", content);
            }
            return success;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            get.releaseConnection();
            if (null != response)
                EntityUtils.consumeQuietly(response.getEntity());
        }
        return false;
    }
}
