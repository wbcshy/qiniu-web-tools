package com.qiniu.utils;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 使用HttpClient类模仿浏览器请求工具类
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.utils
 * @email wangbencheng@qiniu.com
 * @date:2018/12/28
 */
public class HttpClientUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final CloseableHttpClient httpClient;
    public static final String CHARSET = "UTF-8";

    static {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(15000).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    public String doLazyGet(String url, Map<String, Object> params) {
        synchronized (this) {
            try {
                Thread.sleep(300); //延时0.1秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return doGet(url, params, CHARSET);
        }
    }

    public static String doGet(String url, Map<String, Object> params) {
        return doGet(url, params, CHARSET);
    }

    public static String doPost(String url, Map<String, String> params) {
        return doPost(url, params, CHARSET);
    }

    /**
     * HTTP Get 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param params  请求的参数
     * @param charset 编码格式
     * @return 页面内容
     */
    public static String doGet(String url, Map<String, Object> params, String charset) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            if (params != null && !params.isEmpty()) {
//                List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
//                String paramStr = "";
//                for(Map.Entry<String,String> entry : params.entrySet()){
//                    String value = entry.getValue();
//                    if(value != null){
//                        pairs.add(new BasicNameValuePair(entry.getKey(),value));
//                        paramStr += entry.getKey() + "=" + value + "&";
//                    }
//                }
//                if(paramStr.contains("&"))
//                    paramStr = paramStr.substring(0, paramStr.length()-1);
//                url += "?" + paramStr;//EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));

                url = getURL(url, params);
            }

//          logger.info("指数url:" + url);
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpGet.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            logger.error("发起get请求" + e);
        }
        return null;
    }


    /**
     * 这里重载doGet是为了添加请求头信息
     *
     * @param url    完整的请求链接地址
     * @param s      预留参数无意义填空值即可
     * @param header 请求头信息没有 为null
     * @return
     */
    public static String doGet(String url, String s, Map<String, String> header) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        HttpGet httpGet = new HttpGet(url);
        if (header != null) {
            for (Map.Entry<String, String> headerMap : header.entrySet()) {
                httpGet.setHeader(headerMap.getKey(), headerMap.getValue());
            }
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        } catch (Exception e1) {
            logger.error("发起get请求" + e1);
        }
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            httpGet.abort();
            throw new RuntimeException("HttpClient,error status code :" + statusCode);
        }
        try {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            logger.error("发起get请求" + e);
        }
        return null;
    }


    /**
     * @param url 完整的请求链接地址
     * @return
     */
    public static String doGet(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        System.out.println("=========" + url);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
        } catch (Exception e1) {
            logger.error("发起get请求" + e1);
        }
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            httpGet.abort();
            throw new RuntimeException("HttpClient,error status code :" + statusCode);
        }
        try {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            logger.error("发起get请求" + e);
        }
        return null;
    }

    public static String getURL(String p_url, Map<String, Object> params) {
        StringBuilder url = new StringBuilder(p_url);
        if (url.indexOf("?") < 0)
            url.append('?');

        for (String name : params.keySet()) {
            url.append('&');
            url.append(name);
            url.append('=');
            url.append(UrlEncode(String.valueOf(params.get(name))));
        }
        return url.toString().replace("?&", "?");
    }

    /**
     * 对字符串进行 url编码
     *
     * @param url
     * @return
     */
    public static String UrlEncode(String url) {
        try {
            String encodeURL = URLEncoder.encode(url, "UTF-8");
            return encodeURL;
        } catch (UnsupportedEncodingException e) {
            return "Issue while encoding" + e.getMessage();
        }
    }


    /**
     * 对字符串进行 url 解码
     *
     * @param url
     * @return
     */
    public static String UrlDecode(String url) {
        try {
            String prevURL = "";
            String decodeURL = url;
            while (!prevURL.equals(decodeURL)) {
                prevURL = decodeURL;
                decodeURL = URLDecoder.decode(decodeURL, "UTF-8");
            }
            return decodeURL;
        } catch (UnsupportedEncodingException e) {
            return "Issue while decoding" + e.getMessage();
        }
    }


    /**
     * HTTP Post 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param params  请求的参数
     * @param charset 编码格式
     * @return 页面内容
     */
    public static String doPost(String url, Map<String, String> params, String charset) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            List<NameValuePair> pairs = null;
            if (params != null && !params.isEmpty()) {
                pairs = new ArrayList<NameValuePair>(params.size());
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    String value = entry.getValue();
                    if (value != null) {
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
            }
            HttpPost httpPost = new HttpPost(url);
            if (pairs != null && pairs.size() > 0) {
                httpPost.setEntity(new UrlEncodedFormEntity(pairs, CHARSET));
            }

            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * HTTP Post 获取内容
     *
     * @param url          请求的url地址 ?之前的地址
     * @param headerParams 请求头的参数
     * @param bodyParams   请求体的参数
     * @param charset      编码格式
     * @return 页面内容
     */
    @SuppressWarnings("deprecation")
    public static String doPost(String url, Map<String, String> headerParams, Map<String, String> bodyParams, String charset) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            HttpPost post = new HttpPost(url);
            if (bodyParams != null && bodyParams.size() > 0) {
                String contentJsonString = JSON.toJSONString(bodyParams);
                if (contentJsonString != null) {
                    String encoderJson = contentJsonString;
                    StringEntity se = new StringEntity(encoderJson);
                    se.setContentType("application/json");
                    se.setContentEncoding(HTTP.UTF_8);
                    post.setEntity(se);
                    logger.info(EntityUtils.toString(post.getEntity(), "utf-8"));
                }
            }

            if (headerParams != null && !headerParams.isEmpty()) {
                for (Map.Entry<String, String> entry : headerParams.entrySet()) {
                    String value = entry.getValue();
                    if (value != null) {
                        post.addHeader(entry.getKey(), value);
                    }
                }
            }

            logger.info("===post请求信息===" + post.toString());
            CloseableHttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                post.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            response.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String doGet = doGet("https://baidu.com", "", null);
        System.out.println(doGet);
    }

}
