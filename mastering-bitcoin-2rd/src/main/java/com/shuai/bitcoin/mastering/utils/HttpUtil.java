package com.shuai.bitcoin.mastering.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpUtil {

//    private static HttpClient httpClient = HttpClients.createDefault();

    private static HttpClient httpClient;

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
//        setMaxTotal设置连接池的最大连接数
        cm.setMaxTotal(100);
//        setDefaultMaxPerRoute设置每个路由上的默认连接个数
        cm.setDefaultMaxPerRoute(20);
        
        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    /**
     * 发送Get请求
     *
     * @param url
     * @param params
     * @return
     */
    public static String get(String url, List<NameValuePair> params) {
        String body = null;
        try {
            // Get请求
            HttpGet httpget = new HttpGet(url);
            // 设置参数
            String str = EntityUtils.toString(new UrlEncodedFormEntity(params));
            httpget.setURI(new URI(httpget.getURI().toString() + "?" + str));
            // 发送请求
            HttpResponse httpresponse = httpClient.execute(httpget);
            // 获取返回数据
            HttpEntity entity = httpresponse.getEntity();
            body = EntityUtils.toString(entity);
//            if (entity != null) {
//                entity.consumeContent();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }

    public static String get(String url) {
        String body = null;
        try {
            // Get请求
            HttpGet httpget = new HttpGet(url);
            System.out.println("begin");
            long begin = System.currentTimeMillis();
            HttpClient httpClient3 = HttpClients.createDefault();
            HttpResponse response = httpClient3.execute(httpget);
            log.info("cost,{}", System.currentTimeMillis() - begin);
            // 获取返回数据
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 404) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            body = EntityUtils.toString(entity, StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }

    public static InputStream getInputStream(String url) {
        try {
            // Get请求
            HttpGet httpget = new HttpGet(url);

            // 临时
            httpget.setHeader("Host", "sgxg.qjajkj.com");
            httpget.setHeader("Cookie", "wfddsource=; WFLLURL=");
            httpget.setHeader("Upgrade-Insecure-Requests", "1");
            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.70 Safari/537.36");

            System.out.println("begin");
            long begin = System.currentTimeMillis();
            //此处如果new一个新的httpclient，会出现卡主的情况，大概在请求20次左右的时候，会出现这样的情况，应该看看源码，了解下是为什么
            HttpClient httpClient2 = HttpClients.createDefault();
            HttpResponse response = httpClient2.execute(httpget);
            log.info("cost,{}", System.currentTimeMillis() - begin);

            int statusCode = response.getStatusLine().getStatusCode();
            // 此处应该如何设计呢？  todo
            if (statusCode == 404) {
                return null;
            }
            HttpEntity entity = response.getEntity();

            return entity.getContent();

        } catch (Exception e) {
            e.printStackTrace();
            //此处可能出现超时这样的错误，不应该返回null，而应该报错，让方法中断。否则对于应用程序的处理者而言，如果没有很好地处理返回值null，就会出现一些其他的问题，例如在当前的场景中，会跳过一些数据。
            log.error("请求异常，{}", url);
//            throw new BusiException("http请求异常");
            return null;

        }
    }

    public static JSONObject get(String url, Map<String, String> headers, List<NameValuePair> params) {
        String body = null;
        JSONObject jsonObject = null;
        try {
            // Get请求
            HttpGet httpget = new HttpGet(url);
            if (!CollectionUtils.isEmpty(headers)) {
                headers.forEach(httpget::setHeader);

            }
            if (!CollectionUtils.isEmpty(params)) {
                // 设置参数
                String str = EntityUtils.toString(new UrlEncodedFormEntity(params));
                httpget.setURI(new URI(httpget.getURI().toString() + "?" + str));
            }

            HttpResponse httpresponse = httpClient.execute(httpget);
            HttpEntity entity = httpresponse.getEntity();
            body = EntityUtils.toString(entity);
            jsonObject = JSONObject.parseObject(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject doGetJson(String url) {
        JSONObject jsonObject = null;
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, "UTF-8");

                jsonObject = JSONObject.parseObject(result);
            }
            httpGet.releaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject doGetJsonForIp(String url) {
        JSONObject jsonObject = null;
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = null;
        try {
//            https://market.aliyun.com/products/57002002/cmapi018957.html?spm=5176.2020520132.101.22.tWb9q2#sku=yuncode1295700000
            httpGet.setHeader("Authorization", "APPCODE 33a30fa3a0bd4bbaa037e04b223822b2");
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, "UTF-8");

                jsonObject = JSONObject.parseObject(result);
            }
            httpGet.releaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public static JSONObject doPostStr(String url, String outStr) {
        HttpPost httpost = new HttpPost(url);
        JSONObject jsonObject = null;
        httpost.setEntity(new StringEntity(outStr, "UTF-8"));
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpost);
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            jsonObject = JSONObject.parseObject(result);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * Send a POST request with raw string data
     *
     * @param url The target URL
     * @param rawData The raw string data to send
     * @return The response as a string
     */
    public static String postRawData(String url, String rawData) {
        String responseBody = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            // Set the content type to application/x-www-form-urlencoded
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            // Create the request body
            StringEntity entity = new StringEntity(rawData, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            // Execute the request
            HttpResponse response = httpClient.execute(httpPost);

            // Get the response
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
            }

            // Release the connection
            httpPost.releaseConnection();

        } catch (Exception e) {
            log.error("Error sending POST request to {}: {}", url, e.getMessage(), e);
        }
        return responseBody;
    }


}
