package com.qiniu.kodo;

import com.qiniu.common.BasicInfo;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.kodo.qoss.ListBucket;
import com.qiniu.kodo.qoss.ListBucketInfo;
import com.qiniu.kodo.service.BucketServer;
import com.qiniu.kodo.service.impl.BucketServerImpl;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.FileUtil;
import com.qiniu.util.QiniuUtil;
import org.junit.Test;

import java.util.*;

/**
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.kodo
 * @email wangbencheng@qiniu.com
 * @date:2019/2/26
 */
public class BucketServerTest {

    Map<String, String> map = FileUtil.getAccountInfo();
    Configuration configuration = new Configuration();
    BasicInfo basicInfo = new BasicInfo(map.get("ak"), map.get("sk"), configuration);
    KodoManager kodoManager = QiniuUtil.getBucketManager(basicInfo);
    BucketServer bucketServer = new BucketServerImpl(kodoManager);


    /**
     * 批量复制文件测试
     * @throws QiniuException
     */
    @Test
    public void batchCopyTest() throws QiniuException {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map = new HashMap();
        Map<String, String> map1 = new HashMap();
        map.put("frombucket", "test1");
        map.put("fromkey", "aaa.txt");
        map.put("tobucket", "2345");
        map.put("tokey", "aaa.txt");

        map1.put("frombucket", "test1");
        map1.put("fromkey", "demo.js");
        map1.put("tobucket", "2345");
        map1.put("tokey", "demo.js");

        list.add(map);
        list.add(map1);

        Response response = bucketServer.bathCopy(basicInfo, list);
        System.out.println(response.statusCode);
    }

    /**
     * 获取空间列表测试
     */
    @Test
    public void getBucketList() {
        List<String> list = bucketServer.getBucketListInfo(basicInfo);
        for (String result : list) {
            System.out.println(result);
        }
        System.out.println(list.size());
    }

    /**
     * 空间授权测试
     */
    @Test
    public void shareBucketTest() throws QiniuException {
        String ak = "J_4kb-wj9j23VfEgKiHiQZUXOMVRVi2bVqI3Z1jy";
        String sk = "YGPy7O6EG0_aamMfWR2snZVuAlb4UuLwt3LWduyM";
        Configuration configuration = new Configuration();
        BasicInfo basicInfo = new BasicInfo(ak, sk, configuration);

        String bucket = "srws";
        String uid = "1381635336";
        String perm = "2";

        BucketServer bucketServer = new BucketServerImpl(QiniuUtil.getBucketManager(basicInfo));
        Response response = bucketServer.shareBucket(basicInfo, bucket, uid, perm);
        System.out.println(response.statusCode);
        System.out.println(response.bodyString());
    }

    /**
     * 文件列举测试
     */
    @Test
    public void listBucketFileTest() {
        String bucket = "fhcloudds";
        String marker = "";
        String end = ""; //列举的结束标识
        int listNum = 10000;  //每次列举文件个数
        List<String> prefixes = Arrays.asList("urm","ro");  //多前缀指定列表
        List<String> mimeTypes = Arrays.asList("image/jpeg");  //多条件文件类型
        List<int[]> fsizes = Arrays.asList(new int[]{0, 1024 * 3}, new int[]{1024 * 5, 1024 * 6});
//        ListBucket listBucket = new ListBucket(kodoManager, bucket, marker, end, listNum, prefixes);
        ListBucket listBucket = new ListBucket(kodoManager, bucket, marker, end, listNum, prefixes, mimeTypes, fsizes);
        List<FileInfo> fileInfoList = bucketServer.listBucketFile(basicInfo, listBucket, 4);
        for (FileInfo fileInfo: fileInfoList) {
            System.out.println(fileInfo.key + "----------" + fileInfo.mimeType + "---------" + fileInfo.fsize);
        }
    }

    /**
     * 空间信息获取测试
     */
    @Test
    public void listBucketInfoTest() {
        List<String> regions = Arrays.asList("z1");
        ListBucketInfo listBucketInfo = new ListBucketInfo(kodoManager, regions, null, null, null);

    }

}
