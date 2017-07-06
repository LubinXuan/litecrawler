package me.robin.crawler.common;

import me.robin.crawler.Param;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lubin.Xuan on 2017-07-06.
 * {desc}
 */
public class KVStoreClient {
    private static final HttpClient client = HttpClients.createDefault();

    private static HttpHost httpHost = null;

    public static void host(String host, int port) {
        KVStoreClient.httpHost = new HttpHost(host, port);
    }

    public static String get(String key) {

        if (null == httpHost) {
            return null;
        }

        HttpGet httpGet;
        try {
            httpGet = new HttpGet("/cursor?key=" + URLEncoder.encode(key, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        HttpResponse response = null;

        try {
            response = client.execute(httpHost, httpGet);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != response) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
            httpGet.releaseConnection();
        }
    }

    public static void set(String key, Object value) {

        if (null == httpHost) {
            return;
        }

        HttpPut httpPut;
        try {
            httpPut = new HttpPut("/cursor?key=" + URLEncoder.encode(key, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            return;
        }
        HttpResponse response = null;
        try {
            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(new BasicNameValuePair("key", key));
            pairList.add(new BasicNameValuePair("value", String.valueOf(value)));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairList);
            httpPut.setEntity(entity);
            response = client.execute(httpHost, httpPut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != response) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
            httpPut.releaseConnection();
        }
    }
}
