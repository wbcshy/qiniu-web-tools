package com.qiniu.entity;

import com.qiniu.common.Zone;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

/**
 * 封装上传对象实体类
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.entity
 * @email wangbencheng@qiniu.com
 * @date:2019/1/24
 */
public class UploadEntity extends BaseInfoEntity{

    private Auth auth;
    private String UploadToken;
    private UploadManager uploadManager;
    private StringMap policy;

    public UploadEntity(String accessKey, String secretKey, Zone zone) {
        super(accessKey, secretKey, zone);
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public String getUploadToken() {
        return UploadToken;
    }

    public void setUploadToken(String uploadToken) {
        UploadToken = uploadToken;
    }

    public UploadManager getUploadManager() {
        return uploadManager;
    }

    public void setUploadManager(UploadManager uploadManager) {
        this.uploadManager = uploadManager;
    }

    public StringMap getPolicy() {
        return policy;
    }

    public void setPolicy(StringMap policy) {
        this.policy = policy;
    }
}
