package com.qiniu.kodo.service.impl;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.kodo.KodoManager;
import com.qiniu.common.BasicInfo;
import com.qiniu.kodo.qoss.ListBucket;
import com.qiniu.kodo.service.BucketServer;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;

import java.util.List;
import java.util.Map;

/**
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.kodo.service.impl
 * @email wangbencheng@qiniu.com
 * @date:2019/2/26
 */
public class BucketServerImpl implements BucketServer {

    private final KodoManager kodoManager;

    public BucketServerImpl(KodoManager kodoManager){
        this.kodoManager = kodoManager;
    }

    /**
     * 获取空间列表信息
     * @param basicInfo
     * @return
     */
    @Override
    public List<String> getBucketListInfo(BasicInfo basicInfo) {
        List<String> result = null;
        Auth auth = basicInfo.getAuth();
        Configuration configuration = basicInfo.getConfiguration();
        Client client = basicInfo.getClient();
        try {
            result = kodoManager.buckets();
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 批量复制文件
     * @param basicInfo
     * @param mapList
     * @return
     */
    @Override
    public Response bathCopy(BasicInfo basicInfo, List<Map<String, String>> mapList) throws QiniuException {
        Response response = null;
        KodoManager.BatchOperations batchOperations = setBatchOperation(mapList);
        response = kodoManager.batch(batchOperations);
        if (!response.isOK()) {
            throw new QiniuException(response);
        }
        return response;
    }

    /**
     * 空间授权
     * @param basicInfo
     * @param bucket
     * @param uid
     * @param perm
     * @return
     */
    @Override
    public Response shareBucket(BasicInfo basicInfo, String bucket, String uid, String perm) throws QiniuException {
        Response response = null;
        response = kodoManager.setBucketShare(bucket, uid, perm);
        if (!response.isOK()) {
            throw new QiniuException(response);
        }
        return response;
    }

    /**
     * 文件列举实现
     * @param basicInfo   用户鉴权信息
     * @param listBucket  列举实体类
     * @param threads  列举并发线程数
     * @return
     * @throws QiniuException
     */
    @Override
    public List<FileInfo> listBucketFile(BasicInfo basicInfo, ListBucket listBucket, int threads) {
        return listBucket.getFileInfoByThread(threads);
    }

    /**
     * 根据输入的list来获取BatchOperations对象
     * @param list
     * @return
     */
    public KodoManager.BatchOperations setBatchOperation(List<Map<String, String>> list) {
        KodoManager.BatchOperations batchOperations = new KodoManager.BatchOperations();
        list.forEach(map -> batchOperations.addCopyOp(map.get("frombucket"), map.get("fromkey"), map.get("tobucket"), map.get("tokey")));
        return batchOperations;
    }
}
