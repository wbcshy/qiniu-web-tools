package com.qiniu.util;

import com.qiniu.common.BasicInfo;
import com.qiniu.http.Client;
import com.qiniu.kodo.KodoManager;
import com.qiniu.storage.Configuration;

/**
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.util
 * @email wangbencheng@qiniu.com
 * @date:2019/3/1
 */
public class QiniuUtil {

    /**
     * 获取BucketManager对象
     * @param basicInfo
     * @return
     */
    public static KodoManager getBucketManager(BasicInfo basicInfo) {
        Auth auth = basicInfo.getAuth();
        Configuration configuration = basicInfo.getConfiguration();
        Client client = basicInfo.getClient();
        return new KodoManager(auth, configuration);
    }

}
