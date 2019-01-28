package com.qiniu.entity;

import com.qiniu.util.StringMap;

/**
 * 基本Http请求的实体类
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.entity
 * @email wangbencheng@qiniu.com
 * @date:2019/1/24
 */
public class HttpEntity {

    private String host;
    private String contentType;
    private String method;
    private StringMap header;
    private String body;
    private String url;



    public HttpEntity() {}

    public HttpEntity(String host, String url, String method, String contentType, String body) {
        this.host = host;
        this.contentType = contentType;
        this.method = method;
        this.url = url;
        this.body = body;
    }

    public HttpEntity(String host, String url, String method, String contentType, StringMap header, String body) {
        this.host = host;
        this.contentType = contentType;
        this.method = method;
        this.header = header;
        this.body = body;
        this.url = url;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public StringMap getHeader() {
        return header;
    }

    public void setHeader(StringMap header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
