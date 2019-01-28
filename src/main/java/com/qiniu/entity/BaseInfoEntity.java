package com.qiniu.entity;

import com.qiniu.common.Zone;

/**
 * 基本信息实体类
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.entity
 * @email wangbencheng@qiniu.com
 * @date:2019/1/24
 */
public class BaseInfoEntity {

    private String accessKey;
    private String secretKey;
    private Zone zone;
    private String bucket;

    public BaseInfoEntity(String accessKey, String secretKey, Zone zone) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.zone = zone;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
}
