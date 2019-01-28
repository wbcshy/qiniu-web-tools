package com.qiniu.manager;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.entity.UploadEntity;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.manager
 * @email wangbencheng@qiniu.com
 * @date:2019/1/22
 */
public class BucketManager {

    /**
     * 公有空间的下载链接返回
     * @param fileName 文件名称
     * @param domain 域名
     * @return
     */
    public static String getPublicDownloadUrl(String fileName, String domain) {
        String encodeFileName = null;
        try {
            encodeFileName = URLEncoder.encode(fileName,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("%s/%s", domain, encodeFileName);
    }

    /**
     * 私有空间下载链接
     * @param auth ak、sk签名
     * @param fileName 文件名称
     * @param domain 域名
     * @param expireSeconds 过期时间
     * @return
     */
    public static String getPrivateDownloadUrl(Auth auth, String fileName, String domain, Long expireSeconds) {
        String encodeFileName = null;
        try {
            encodeFileName = URLEncoder.encode(fileName,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String publicUrl = String.format("%s/%s", domain, encodeFileName);
        return  auth.privateDownloadUrl(publicUrl, expireSeconds);
    }

    /**
     * 新建一个UploadManager上传对象
     * @param accessKey
     * @param secretKey
     * @param zone
     * @return
     */
    public static UploadEntity getUploadEntity(String accessKey, String secretKey, StringMap policy, Zone zone) {
        UploadEntity uploadEntity = new UploadEntity(accessKey, secretKey, zone);
        Auth auth = Auth.create(accessKey, secretKey);
        if (null == zone) zone = Zone.autoZone();
        Configuration configuration = new Configuration(zone);
        uploadEntity.setUploadManager(new UploadManager(configuration));
        uploadEntity.setAuth(auth);
        uploadEntity.setPolicy(policy);
        return uploadEntity;
    }


    /**
     * 简单上传
     * @param uploadEntity
     * @param filePath
     * @param key
     * @return
     */
    public static JSONObject simpleUpload(UploadEntity uploadEntity, String filePath,  String key) {
        String simpleUploadToken = uploadEntity.getAuth().uploadToken(uploadEntity.getBucket(), key);
        JSONObject jsonObject = new JSONObject();
        try {
            Response response = uploadEntity.getUploadManager().put(filePath, key, simpleUploadToken);
            jsonObject.put("body", response.bodyString());
        } catch (QiniuException e) {
            Response eResponse = e.response;
            try {
                jsonObject.put("body", eResponse.bodyString());
            } catch (QiniuException e1) {
            }
        }
        return jsonObject;
    }





}
