package com.qiniu.http;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.StringUtils;

/**
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.http
 * @email wangbencheng@qiniu.com
 * @date:2019/3/1
 */
public class HttpManager {

    /**
     * Auth 对象
     * 该类需要使用QBox鉴权，所以需要指定Auth对象
     */
    private final Auth auth;

    /**
     * Configuration 对象
     * 该类相关的域名配置，解析配置，HTTP请求超时时间设置等
     */
    private Configuration configuration;

    /**
     * HTTP Client 对象
     * 该类需要通过该对象来发送HTTP请求
     */
    private final Client client;

    /**
     * 构建一个新的 BucketManager 对象
     *
     * @param auth Auth对象
     * @param cfg  Configuration对象
     */
    public HttpManager(Auth auth, Configuration cfg) {
        this.auth = auth;
        this.configuration = cfg.clone();
        client = new Client(this.configuration);
    }

    public HttpManager(Auth auth, Client client) {
        this.auth = auth;
        this.client = client;
    }

    /*
     * 相关请求的方法列表
     * */
    public Response rsPost(String bucket, String path, byte[] body) throws QiniuException {
        check(bucket);
        String url = configuration.rsHost(auth.accessKey, bucket) + path;
        return post(url, body);
    }

    public Response rsGet(String bucket, String path) throws QiniuException {
        check(bucket);
        String url = configuration.rsHost(auth.accessKey, bucket) + path;
        return get(url);
    }

    public Response ioPost(String bucket, String path) throws QiniuException {
        check(bucket);
        String url = configuration.ioHost(auth.accessKey, bucket) + path;
        return post(url, null);
    }

    public Response pubPost(String path) throws QiniuException {
        String url = "http://pu.qbox.me:10200" + path;
        return post(url, null);
    }

    public Response get(String url) throws QiniuException {
        StringMap headers = auth.authorization(url);
        return client.get(url, headers);
    }

    public Response post(String url, byte[] body) throws QiniuException {
        StringMap headers = auth.authorization(url, body, Client.FormMime);
        return client.post(url, body, headers, Client.FormMime);
    }

    public void check(String bucket) throws QiniuException {
        if (StringUtils.isNullOrEmpty(bucket)) {
            throw new QiniuException(Response.createError(null, null, 0, "未指定操作的空间或操作体为空"));
        }
    }
}
