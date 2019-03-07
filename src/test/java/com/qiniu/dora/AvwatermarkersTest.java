package com.qiniu.dora;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.processing.OperationManager;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import com.qiniu.util.UrlSafeBase64;
import org.junit.Test;

/**
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.dora
 * @email wangbencheng@qiniu.com
 * @date:2019/3/1
 */
public class AvwatermarkersTest {

    @Test
    public void testAvwatermakersTest() {
        //设置好账号的ACCESS_KEY和SECRET_KEY
        String ACCESS_KEY = "";
        String SECRET_KEY = "";
        //要上传的空间
        String bucketname = "imc-hd";
        //上传文件的路径  https://image.bcwgel.com/douguo.mp4
        //目标样式：https://image.bcwgel.com/douguo.mp4?avwatermarks/mp4/wmImage/aHR0cHM6Ly9pbWFnZS5iY3dnZWwuY29tL2RvdWd1by5wbmc=/wmGravity/NorthWest/wmOffsetX/10/wmOffsetY/10/wmText/QHR1ZG91/wmGravityText/NorthWest/wmFontSize/32/offsetX/10/wmOffsetY/80
        String key = "douguo.mp4";
        //上传后的文件名
        String newKey = "douguo-new.mp4";


        //设置转码的队列
        String pipeline = "test-1";
        String imgUrl = "https://image.bcwgel.com/douguo.png";
        String text = "@tudou";

        //设置转码操作参数
        String fops = String.format("avwatermarks/mp4/wmImage/%s/");

        //可以对转码后的文件进行使用saveas参数自定义命 名，当然也可以不指定文件会默认命名并保存在当前空间。
        String urlbase64 = UrlSafeBase64.encodeToString("test1:" + newKey);
        String pfops = fops + "|saveas/" + urlbase64;


        //密钥配置
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

        //第二种方式: 自动识别要上传的空间(bucket)的存储区域是华东、华北、华南。
        Zone z = Zone.autoZone();
        Configuration c = new Configuration(z);

        OperationManager operationManager = new OperationManager(auth, c);

        String id = null;
        try {
            id = operationManager.pfop(bucketname, key, pfops, pipeline, true);
        } catch (QiniuException e) {
            e.printStackTrace();
        }

        System.out.println(id);
    }

}
