package com.qiniu.common;

import com.qiniu.http.Client;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;

/**
 * kodo相关需要的实体类
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.kodo.model
 * @email wangbencheng@qiniu.com
 * @date:2019/2/26
 */
public class BasicInfo {

    private String ak;
    private String sk;
    private Auth auth;
    private Configuration configuration;
    private Client client;

    public BasicInfo(String ak, String sk, Configuration configuration) {
        this.ak = ak;
        this.sk = sk;
        auth = Auth.create(ak, sk);
        this.configuration = configuration;
        client = new Client(configuration);
    }

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public Auth getAuth() {

        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
