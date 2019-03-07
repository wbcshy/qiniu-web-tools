package com.qiniu.kodo;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;

/**
 * 在七牛sdk基础上额外封装实现的api功能
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.kodo
 * @email wangbencheng@qiniu.com
 * @date:2019/2/26
 */
public class KodoManager extends BucketManager {

    private final Auth auth;

    private Configuration configuration;

    private final Client client;

    public KodoManager(Auth auth, Configuration cfg) {
        super(auth, cfg);
        this.auth = auth;
        this.configuration = cfg;
        client = new Client(this.configuration);
    }

    public KodoManager(Auth auth, Client client) {
        super(auth, client);
        this.auth = auth;
        this.client = client;
    }

    /**
     * 空间授权
     * @param bucket 要授权的空间
     * @param uid 目标用户id
     * @param name 授权后空间名字，为空与原空间相同
     * @param perm 授予权限
     * @return
     */
    public Response setBucketShare(String bucket, String uid, String name, String perm) throws QiniuException {
        String url = String.format("%s/share/%s/to/%s/name/%s/perm/%s", configuration.apiHost(), bucket, uid, name, perm);
        Response response = this.post(url, null);
        if (!response.isOK()) {
            throw new QiniuException(response);
        }
        response.close();
        return response;
    }

    /**
     * 空间授权，不带name
     * @param bucket
     * @param uid
     * @param perm
     * @return
     * @throws QiniuException
     */
    public Response setBucketShare(String bucket, String uid, String perm) throws QiniuException {
        String url = String.format("%s/share/%s/to/%s/perm/%s", configuration.apiHost(), bucket, uid, perm);
        Response response = this.post(url, null);
        if (!response.isOK()) {
            throw new QiniuException(response);
        }
        response.close();
        return response;
    }




}
