package com.qiniu.kodo.qoss;

import com.qiniu.common.QiniuException;
import com.qiniu.kodo.KodoManager;
import com.qiniu.kodo.model.BucketInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 列举七牛空间相关的信息
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.kodo.qoss
 * @email wangbencheng@qiniu.com
 * @date:2019/3/11
 */
public class ListBucketInfo {

    private final KodoManager kodoManager;

    private final List<String> regions;   //指定哪几个区域的存储空间

    private final String line;

    private final String global;

    private final String share;

    private List<BucketInfo> bucketInfoList;

    public ListBucketInfo(KodoManager kodoManager, List<String> regions, String global, String line, String share) {
        this.kodoManager = kodoManager;
        this.regions = regions == null ? Arrays.asList("") : regions;
        this.global = global;
        this.line = line;
        this.share = share;
        this.bucketInfoList = getBucketInfoByRegions(regions);
    }

    public List<BucketInfo> getBucketInfoList() {
        return this.bucketInfoList;
    }

    public List<BucketInfo> getBucketInfoByRegions(List<String> regions) {
        List<String> reponseList = regions.stream().map(region -> {
            String result = null;
            try {
                result = kodoManager.getBucketListByRegion(region, global, line, share).bodyString();
            } catch (QiniuException e) {
                e.printStackTrace();
            }
            return result;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println(reponseList.get(0));
        return null;
    }

    private List<BucketInfo> fromBucketInfos(String response) {

        return null;
    }

}
