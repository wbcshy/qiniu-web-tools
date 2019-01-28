package com.qiniu.demo;

import com.qiniu.common.Zone;
import com.qiniu.entity.BaseInfoEntity;
import com.qiniu.entity.HttpEntity;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.utils.HttpClientUtil;
import com.qiniu.utils.QiniuUtils;

/**
 * 测试列举支持http/https的域名
 * 包含：
 * rs.qiniu.com
 * rsf.qiniu.com
 * api.qiniu.com
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.demo
 * @email wangbencheng@qiniu.com
 * @date:2019/1/22
 */
public class QiniuRegionHttpTest {

    public static HttpEntity httpEntity;

    public static String testRs(BaseInfoEntity baseInfoEntity) {
        String qiniuFlag = "QBox";
        String url = "http://uc.qbox.me/private";
        String host = "uc.qbox.me";
        String method = "POST";
        String contentType = "application/x-www-form-urlencoded";
        String body = "bucket=" + "test1" + "&private=" + 0;//0：公有 1：私有
        Auth auth = Auth.create(baseInfoEntity.getAccessKey(), baseInfoEntity.getSecretKey());
        httpEntity = new HttpEntity(host, url, method, contentType, body);
        String sign = QiniuUtils.getQiniuTokenSign(auth, qiniuFlag, httpEntity);
        System.out.println("sign： " + sign);
        StringMap header = new StringMap();
        header.put("Host", host);
        header.put("Authorization", sign);
        header.put("Content-Type", contentType);
        Configuration configuration = new Configuration(baseInfoEntity.getZone());
        httpEntity.setBody(body);
        httpEntity.setHeader(header);
        return QiniuUtils.getResponseStr(configuration, httpEntity);

    }

    public static void main(String[] args) {
        String ak = "wI7zBljGtNLoMYA9_A1AYLyNIe-r1pV_-ck3zSmA";
        String sk = "0XBsASDKPieeYd9G7yhjfG-mumoc-SRXRQ0tp57A";
        Zone zone = Zone.zone0();
        BaseInfoEntity baseInfoEntity = new BaseInfoEntity(ak, sk, zone);
        System.out.println(testRs(baseInfoEntity));
    }

}
