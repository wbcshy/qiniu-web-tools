package com.qiniu.kodo.model;

import com.qiniu.kodo.KodoManager;
import com.qiniu.storage.model.FileInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 批量操作时的过滤条件
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.kodo.model
 * @email wangbencheng@qiniu.com
 * @date:2019/3/7
 */
public class BatchFilter {

    private final KodoManager kodoManager;
    private final String fromBucket;   //原文件空间
    private final String toBucket;   //目标文件空间
    private final List<FileInfo> fileInfoList;   //要操作的文件
    private List<Map<String, String>> batchMap;


    /**
     * BatchFilter的构造函数
     * @param kodoManager
     * @param fromBucket
     * @param toBucket
     * @param fileInfoList
     */
    public BatchFilter(KodoManager kodoManager, String fromBucket, String toBucket, List<FileInfo> fileInfoList) {
        this.kodoManager = kodoManager;
        this.fromBucket = fromBucket;
        this.toBucket = toBucket;
        this.fileInfoList = fileInfoList == null ? new ArrayList<>() : fileInfoList;
        this.batchMap = getResultMap();
    }

    /**
     * 获取批量操作的Map对象
     * @return
     */
    private List<Map<String, String>> getResultMap() {
        List<Map<String, String>> map = fileInfoList.parallelStream()
                .map(fileInfo -> {
                    Map<String, String> maps = new HashMap();
                    maps.put("frombucket",fromBucket);
                    maps.put("fromkey", fileInfo.key);
                    maps.put("tobucket", toBucket);
                    maps.put("tokey", fileInfo.key);
                    return maps;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return map;
    }


    /**
     * 获取文件名称
     * @param fileInfos
     * @return
     */
    private List<String> getKeyByFileInfo(List<FileInfo> fileInfos) {
        return fileInfos.parallelStream()
                .map(fileInfo -> fileInfo.key)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取文件类型
     * @param fileInfos
     * @return
     */
    private List<String> getMimeTypeByFileInfo(List<FileInfo> fileInfos) {
        return fileInfos.parallelStream()
                .map(fileInfo -> fileInfo.mimeType)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


}
