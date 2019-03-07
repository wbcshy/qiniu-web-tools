package com.qiniu.dora;

import com.qiniu.http.Client;
import com.qiniu.http.HttpManager;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;

/**
 * 封装Dora产品实现的一些api功能
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.dora
 * @email wangbencheng@qiniu.com
 * @date:2019/2/28
 */
public class DoraManager extends HttpManager {

    private final Auth auth;

    private Configuration configuration;

    private Client client;

    public DoraManager(Auth auth, Configuration configuration) {
        super(auth, configuration);
        this.auth = auth;
        this.configuration = configuration;
        client = new Client(this.configuration);
    }

    public DoraManager(Auth auth, Client client) {
        super(auth, client);
        this.auth = auth;
        this.client = client;
    }


    public Response avWaterMarkers() {
        Response response = null;
        return response;
    }



}
