package com.qiniu.utils;

import com.qiniu.common.QiniuException;
import com.qiniu.entity.HttpEntity;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import com.qiniu.util.Base64;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 七牛相关的工具类
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.utils
 * @email wangbencheng@qiniu.com
 * @date:2019/1/22
 */
public class QiniuUtils {

    /**
     * 使用signRequest方法获取签名
     * @param auth ak、sk签名
     * @param qiniuFlag 使用Api接口标志，如Qiniu等
     * @param httpEntity http实体类
     * @return
     */
    public static String getQiniuTokenSign(Auth auth, String qiniuFlag, HttpEntity httpEntity) {
        return qiniuFlag + " "  + auth.signRequest(httpEntity.getUrl(), httpEntity.getBody().getBytes(), httpEntity.getContentType());
    }

    /**
     * 使用signRequestV2方法获取签名
     * @param auth ak、sk签名
     * @param qiniuFlag 使用Api接口标志，如Qiniu等
     * @param httpEntity http实体类
     * @return
     */
    public static String getQiniuTokenSignV2(Auth auth, String qiniuFlag, HttpEntity httpEntity) {
        return qiniuFlag + " "  + auth.signRequestV2(httpEntity.getUrl(), httpEntity.getMethod(), httpEntity.getBody().getBytes(), httpEntity.getContentType());
    }

    /**
     * 发送请求给七牛服务获取返回结果
     * @param configuration
     * @param httpEntity
     * @return
     */
    public static Response getResponseStr(Configuration configuration, HttpEntity httpEntity) {
        Client client = new Client(configuration);
        Response response = null;
        if ("POST".equals(httpEntity.getMethod())) {
            try {
                response = client.post(httpEntity.getUrl(), httpEntity.getBody().getBytes(), httpEntity.getHeader(),httpEntity.getContentType());
            } catch (QiniuException e) {
                response = e.response;
            }
        }
        if ("GET".equals(httpEntity.getMethod())) {
            try {
                response = client.get(httpEntity.getUrl(), httpEntity.getHeader());
            } catch (QiniuException e) {
                response = e.response;
            }
        }
        return response;
    }



}
