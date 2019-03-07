package com.qiniu.kodo.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.common.BasicInfo;
import com.qiniu.kodo.qoss.ListBucket;
import com.qiniu.storage.model.FileInfo;

import java.util.List;
import java.util.Map;

/**
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.kodo.service
 * @email wangbencheng@qiniu.com
 * @date:2019/2/26
 */
public interface BucketServer {

    /**
     * 获取空间列表信息
     * @param basicInfo
     * @return
     */
    List<String> getBucketListInfo(BasicInfo basicInfo);

    /**
     * 批量复制文件
     * @param basicInfo
     * @param mapList
     * @return
     */
    Response bathCopy(BasicInfo basicInfo, List<Map<String, String>> mapList) throws QiniuException;

    /**
     * 空间授权
     * @param basicInfo
     * @param bucket
     * @param uid
     * @param perm
     * @return
     */
    Response shareBucket(BasicInfo basicInfo, String bucket, String uid, String perm) throws QiniuException;


    /**
     * 文件列举
     * @param basicInfo
     * @param listBucket
     * @return
     * @throws QiniuException
     */
    List<FileInfo> listBucketFile(BasicInfo basicInfo, ListBucket listBucket, int threads);

}
