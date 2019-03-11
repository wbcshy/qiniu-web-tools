package com.qiniu.kodo.qoss;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.kodo.KodoManager;
import com.qiniu.kodo.model.FileLister;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.util.Base64;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 七牛空间文件并发列举实现功能
 *
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.kodo.qoss
 * @email wangbencheng@qiniu.com
 * @date:2019/3/2
 */
public class ListBucket {
    private final KodoManager kodoManager;
    private final String bucket;
    private final String marker;

    private final String end; //列举的结束标识
    private final int listNum;  //每次列举文件个数
    private final List<String> prefixes;  //多前缀指定列表
    private List<String> mimeTypes;   //多文件类型指定
    private List<int[]> fsizes;   //指定文件区间大小 [a,b]的形式


    /**
     * ListBucket的构造函数 不指定文件类型
     *
     * @param bucket
     * @param marker
     * @param end
     * @param listNum
     * @param prefixes
     */
    public ListBucket(KodoManager kodoManager, String bucket, String marker, String end, int listNum, List<String> prefixes) {
        this.kodoManager = kodoManager;
        this.bucket = bucket;
        this.marker = marker;
        this.end = end;
        this.listNum = listNum;
        this.prefixes = prefixes == null ? new ArrayList<>() : prefixes;
    }

    /**
     * ListBucket构造函数 指定文件类型 和文件大小范围
     *
     * @param kodoManager
     * @param bucket
     * @param marker
     * @param end
     * @param listNum
     * @param prefixes
     * @param mimeTypes
     */
    public ListBucket(KodoManager kodoManager, String bucket, String marker, String end, int listNum, List<String> prefixes,
                      List<String> mimeTypes, List<int[]> fsizes) {
        this.kodoManager = kodoManager;
        this.bucket = bucket;
        this.marker = marker;
        this.end = end;
        this.listNum = listNum;
        this.prefixes = prefixes == null ? new ArrayList<>() : prefixes;
        this.mimeTypes = mimeTypes == null ? Arrays.asList("") : mimeTypes;
        this.fsizes = fsizes == null ? Arrays.asList(new int[]{0,0}) : fsizes;
    }

    /**
     * 通过传入多前缀、多文件类型和文件区间大小进行筛选
     *
     * @param prefixes
     * @param listNum
     * @return
     */
    public List<FileLister> getFileListByAll(List<String> prefixes, int listNum) {
        List<List<FileLister>> resultList = prefixes.parallelStream()
                .map(prefix -> {
                    List<FileLister> fileListerList = new ArrayList<>();
                    List<FileLister> fileListers = new ArrayList<>();
                    FileLister fileLister = null;
                    try {
                        //文件类型不为空
                        if (mimeTypes != null) {
                            for (String mimeType : mimeTypes) {
                                //文件大小不为空
                                if (fsizes != null) {
                                    for (int[] fsize : fsizes) {
                                        fileLister = new FileLister(kodoManager, bucket, prefix, mimeType, fsize, marker, end, null, listNum);
                                        fileListers.add(fileLister);
                                    }
                                } else {
                                    fileLister = new FileLister(kodoManager, bucket, prefix, mimeType, null, marker, end, null, listNum);
                                    fileListers.add(fileLister);
                                }
                            }
                            fileListerList.addAll(fileListers);
                        }else {
                            fileLister = new FileLister(kodoManager, bucket, prefix, null, null, marker, end, null, listNum);
                            fileListerList.add(fileLister);
                        }
                    } catch (QiniuException e) {
                        e.printStackTrace();
                    }
                    return fileListerList;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<FileLister> fileListerLists = new ArrayList<>();
        for (List<FileLister> fileListerList : resultList) {
            List<FileLister> fileLispers = new ArrayList<>();
            for (FileLister fileLister : fileListerList) {
                fileLispers.add(fileLister);
            }
            fileListerLists.addAll(fileLispers);
        }
        return fileListerLists;
    }


    /**
     * 根据自定义的prefix前缀来进一步过滤列举,去除部分不满足接口实现的前缀格式
     *
     * @param customPrefix 自定义的前缀
     * @return
     */
    public List<FileLister> handelFileListBySinglePrefix(String customPrefix) {
        //添加接口支持的ASCII表包含字符的前缀，避免列举时出现接口超时问题
        List<String> originPrefixList = new ArrayList<>();
        originPrefixList.addAll(Arrays.asList(("./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRST").split("")));
        originPrefixList.addAll(Arrays.asList(("UVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz").split("")));
        //添加自定义前缀到过滤条件列表中
        List<String> customPrefixList = new ArrayList<String>() {{
            add(customPrefix);
        }};
        List<FileLister> fileListerList = getFileListByAll(customPrefixList, listNum);
        List<FileLister> filterFileListerList = new ArrayList<>();
        filterFileListerList.addAll(fileListerList);
        //添加第一段FileLister, 并设置结束的标识 endKeyPrefix，即为最后一段的FileLister设置前缀prefix和开始marker
        if (fileListerList.size() > 1) {
//            filterFileListerList.addAll(getFileListByPrefixes(new ArrayList<String>(){{add(customPrefix);}}, listNum));
            filterFileListerList.sort(Comparator.comparing(FileLister::getPrefix));
            filterFileListerList.get(0).setEndKeyPrefix(filterFileListerList.get(1).getPrefix());
            FileLister fileLister = filterFileListerList.get(filterFileListerList.size() - 1);   //最后一段的FileLister
            fileLister.setPrefix(customPrefix);
            if (!fileLister.checkMarkerValid()) {
                FileInfo lastFileInfo = fileLister.getFileInfoList().parallelStream()
                        .filter(Objects::nonNull)
                        .max(Comparator.comparing(fileInfo -> fileInfo.key)).orElse(null);
                fileLister.setMarker(calcMarker(lastFileInfo));
            }
        }
        return filterFileListerList;
    }

    /**
     * 执行列举操作,获取文件信息
     *
     * @param fileLister
     */
    public List<FileInfo> executeFileLister(FileLister fileLister) {
        List<FileInfo> fileInfoLists = new ArrayList<>();
        while (fileLister.hasNext()) {
            List<FileInfo> fileInfoList = fileLister.next();
            while (fileLister.exception != null) {   //    列举出错时候
                System.out.println("list prefix:" + fileLister.getPrefix() + " retrying...");
                fileLister.exception = null;
                fileInfoList = fileLister.next();
            }
            fileInfoLists.addAll(fileInfoList);
        }
        return fileInfoLists;
    }

    /**
     * 通过线程池获取文件信息
     *
     * @param executorPool   传入的线程池对象
     * @param fileListerList FileLister列表
     * @return
     */
    public List<FileInfo> getFileListByThreadPool(ExecutorService executorPool, List<FileLister> fileListerList) {
        List<FileInfo> resultList = new ArrayList<>();
        for (int i = 0; i < fileListerList.size(); i++) {
            FileLister fileLister = fileListerList.get(i);
            //线程池执行
//            executorPool.execute(() -> {
//                List<FileInfo> fileInfoList = executeFileLister(fileLister);
//                resultList.addAll(fileInfoList);
//            });
            //测试时使用
            List<FileInfo> fileInfoList = executeFileLister(fileLister);
            resultList.addAll(fileInfoList);
        }
        return resultList;
    }

    /**
     * 通过传入的线程数启动线程池执行列举任务
     *
     * @param threads
     */
    public List<FileInfo> getFileInfoByThread(int threads) {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<FileLister> fileListerList = new ArrayList<>();
        ;
        if (prefixes.size() == 0) {  //表示列举全部文件
            fileListerList = handelFileListBySinglePrefix("");
        } else {  //输入前缀条件时，执行筛选过滤来列举文件
            for (int i = 0; i < prefixes.size(); i++) {
                fileListerList.addAll(handelFileListBySinglePrefix(prefixes.get(i)));
                // 为第一段 FileLister 设置结束标志 EndKeyPrefix，及为最后一段 FileLister 修改前缀 prefix 和开始 marker
                if (fileListerList.size() > 1) {
                    fileListerList.sort(Comparator.comparing(FileLister::getPrefix));
                }
            }
        }
        return getFileListByThreadPool(executorService, fileListerList);
    }

    /**
     * 根据FileInfo对象计算maeker字符串
     *
     * @param fileInfo
     * @return
     */
    public String calcMarker(FileInfo fileInfo) {
        if (fileInfo == null) return null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("c", fileInfo.type);
        jsonObject.addProperty("k", fileInfo.key);
        Gson gson = new Gson();
        String markerJson = gson.toJson(jsonObject);
        return com.qiniu.util.Base64.encodeToString(markerJson.getBytes(Constants.UTF_8), com.qiniu.util.Base64.URL_SAFE | Base64.NO_WRAP);
    }

}
