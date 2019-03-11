package com.qiniu.kodo.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.kodo.KodoManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.JsonConvertUtils;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 列举空间文件一段的列举实体
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.kodo.model
 * @email wangbencheng@qiniu.com
 * @date:2019/2/28
 */
public class FileLister implements Iterator<List<FileInfo>> {


    private KodoManager kodoManager;
    private String bucket;   //目标空间
    private String prefix;   //资源前缀
    private String mimeType;   //资源类型
    private int[] fsize;   //资源大小区间  [a]:表示一个读固定文件大小值  [a,b]:表示文件大小范围值
    private String delimter;   //仿照目录集做分割
    private String marker;   //记录标识
    private String endKeyPrefix;   //表示列举的结束表识
    private int limit;  //一段列举的数量
    private List<FileInfo> fileInfoList;  //资源信息
    public QiniuException exception;

    public FileLister(KodoManager kodoManager, String bucket, String prefix, String mimeType, int[] fsize, String marker,
                      String endKeyPrefix, String delimter, int limit) throws QiniuException {
        this.kodoManager = kodoManager;
        this.bucket = bucket;
        this.prefix = prefix;
        this.fsize = fsize;
        this.mimeType = mimeType;
        this.marker = marker;
        this.endKeyPrefix = endKeyPrefix == null ? "" : endKeyPrefix;
        this.delimter = delimter;
        this.limit = limit;
        this.fileInfoList = getFileInfoResult(prefix, delimter, marker, limit);
    }

    public String getMarker() {
        return marker;
    }
    public String getPrefix() {
        return prefix;
    }
    public List<FileInfo> getFileInfoList() {
        return fileInfoList;
    }
    public void setEndKeyPrefix(String endKeyPrefix) {
        this.endKeyPrefix = endKeyPrefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public void setMarker(String marker) {
        this.marker = marker;
    }

    /**
     * 列举资源，获取到文件信息内容
     * 列举数据格式
     * {"item":{"key":"files/D/93.log","hash":"FhWqsP2Lk36zuwGEFpPzXct12i-v","fsize":6,"mimeType":"application/octet-stream","putTime":15148601207652690,"type":0,"status":0},"marker":"eyJjIjowLCJrIjoiZmlsZXMvRC85My5sb2cifQ==","dir":""}
     * {"item":null,"marker":"eyJjIjowLCJrIjoiZmlsZXMvRC85NC5sb2cifQ==","dir":""}
     * {"item":null,"marker":"eyJjIjowLCJrIjoiZmlsZXMvIn0=","dir":"files/"}
     * {"item":null,"marker":"","dir":"mobile/"}
     * @param prefix
     * @param delimiter
     * @param marker
     * @param limit
     * @return
     * @throws QiniuException
     */
    private List<FileInfo> getFileInfoResult(String prefix, String delimiter, String marker, int limit) throws QiniuException {
        Response response = kodoManager.listV2(bucket, prefix, marker, limit, delimiter);
        InputStream inputStream = new BufferedInputStream(response.bodyStream());
        Reader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        //将响应的数据流每行都转换成字符串存入List中
        List<String> resultList = bufferedReader.lines()
                .filter(line -> !StringUtils.isNullOrEmpty(line))
                .collect(Collectors.toList());
        //将List<String>   --->  List<FileListLine>
        List<FileListLine> fileListLines = resultList.parallelStream()
                .map(fileListLine -> new FileListLine().fromFileListLine(fileListLine))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        response.close();
        //处理FileListLine，转换过程中可能出现直接返回空列表情况，marker不做修改，返回后在使用同样的maker来列举即可
        if (fileListLines.size() < resultList.size())
            return new ArrayList<>();
        //获取FileInfo的List，过滤到不为空的列举文件信息
        List<FileInfo> fileInfoList = fileListLines.parallelStream()
                .map(fileListLint -> fileListLint.fileInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        //文件类型的筛选列举
        if (mimeType != null && !"".equals(mimeType)) {
            fileInfoList = fileInfoList.stream().filter(fileInfo -> fileInfo.mimeType.equals(mimeType)).collect(Collectors.toList());
        }
        //根据文件大小来筛选列举
        if (fsize != null && fsize.length > 0 && fsize[1] != 0) {
            fileInfoList = fileInfoList.stream().filter(fileInfo -> {
                boolean result = false;
                if (fsize.length == 1)
                    result = fileInfo.fsize == fsize[0] ? true : false;
                if (fsize.length == 2)
                    result = (fileInfo.fsize >= fsize[0] && fileInfo.fsize <= fsize[1]) ? true : false;
                return result;
            }).collect(Collectors.toList());
        }

        Optional<FileListLine> lastFileListLine = fileListLines.parallelStream().max(FileListLine::compareTo);
        this.marker = lastFileListLine.map(fileListLine -> fileListLine.marker).orElse("");
        return fileInfoList;
    }

    @Override
    public boolean hasNext() {
        return checkMarkerValid() || checkFileListValid();
    }

    /**
     * 列举是否直到元素最后一个
     * @return
     */
    @Override
    public List<FileInfo> next() {
        List<FileInfo> currentList = fileInfoList == null ? new ArrayList<>() : fileInfoList;
        if (!"".equals(endKeyPrefix)) {
            int size = currentList.size();
            currentList = currentList.parallelStream()
                    .filter(fileInfo -> fileInfo.key.compareTo(endKeyPrefix) < 0)  //表示列举文件名还不是最后一个文件名
                    .collect(Collectors.toList());
            int finalSize = currentList.size();
            if (finalSize < size) marker = null;
        }
        if (!checkMarkerValid()) {
            fileInfoList = null;
        } else {
            try {
                do {
                    fileInfoList = getFileInfoResult(prefix, delimter, marker, limit);
                } while (!checkMarkerValid() && !checkFileListValid());
            } catch (QiniuException e) {
                fileInfoList = null;
                exception = new QiniuException(e);
            }
        }
        return currentList;
    }

    @Override
    public void remove() {
        this.kodoManager = null;
        this.fileInfoList = null;
        this.exception = null;
    }

    public boolean checkMarkerValid() {
        return marker != null && !"".equals(marker);
    }

    public boolean checkFileListValid() {
        return fileInfoList != null && fileInfoList.size() > 0;
    }

    public class FileListLine implements Comparable<FileListLine>{
        public String marker = "";
        public String dir = "";
        public FileInfo fileInfo;   //相当于一个item对象

        /**
         * 将响应的内容转换成FileListLine对象
         * <p>
         * 响应的item表示一个文件
         * 响应的marker表示当前list的位置，客户端应该记录下来，如果list没结束，下一次list带上最后一个marker
         * 响应的dir只在请求里面指定delimiter才有效，表示一个目录
         * </p>
         * @param line 响应内容的字符串内容
         * @return
         */
        public FileListLine fromFileListLine(String line) {
            if (!StringUtils.isNullOrEmpty(line)) {
                JsonObject jsonObject = JsonConvertUtils.toJsonObject(line);   //将字符串内容转换成json对象
                JsonElement item = jsonObject.get("item");
                JsonElement marker = jsonObject.get("marker");
                JsonElement dir = jsonObject.get("dir");
                if (item != null && !(item instanceof JsonNull)) {
                    this.fileInfo = JsonConvertUtils.fromJson(item, FileInfo.class);
                }
                if (marker != null && !(marker instanceof  JsonNull)) {
//                    this.marker = JsonConvertUtils.fromJson(marker, String.class);
                    this.marker = marker.getAsString();
                }
                if (dir != null && !(dir instanceof JsonNull)) {
                    this.dir = dir.getAsString();
                }
            }
            return this;
        }


        /**
         * 因为并行流的原因，导致collect转list时候，不确保最后一个元素是实际列举的最后一个记录，所以对所有的元素进行compare
         * @param fileListLine
         * @return
         */
        @Override
        public int compareTo(FileListLine fileListLine) {
            if (this.fileInfo == null && fileListLine.fileInfo == null) {
                return 0;
            } else if (this.fileInfo == null) {
                if (!"".equals(marker) && marker != null) {
                    String markerJson = new String(UrlSafeBase64.decode(marker));   //将marker使用base64解码
                    String key = JsonConvertUtils.fromJson(markerJson, JsonObject.class).get("k").getAsString();
                    return key.compareTo(fileListLine.fileInfo.key);
                }
                return 1;
            } else if (fileListLine.fileInfo == null) {
                if (!"".equals(fileListLine.marker) && fileListLine.marker != null) {
                    String markerJson = new String(UrlSafeBase64.decode(fileListLine.marker));
                    String key = JsonConvertUtils.fromJson(markerJson, JsonObject.class).get("k").getAsString();
                    return this.fileInfo.key.compareTo(key);
                }
                return -1;
            }else {
                return this.fileInfo.key.compareTo(fileListLine.fileInfo.key);
            }
        }
    }


}
