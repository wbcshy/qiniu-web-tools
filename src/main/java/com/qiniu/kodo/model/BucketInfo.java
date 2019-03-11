package com.qiniu.kodo.model;

/**
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.kodo.model
 * @email wangbencheng@qiniu.com
 * @date:2019/3/11
 */
public class BucketInfo {

    public String itbl;

    public String phy;

    public String tbl;

    /**
     * 空间所处区域
     */
    public String region;

    /**
     * 是否是全局空间
     */
    public String global;

    /**
     * 是否是低频存储
     */
    public String line;

    public String ouid;

    public String oitbl;

    public String otbl;

    /**
     * 空间权限
     */
    public String perm;

    /**
     * 空间授权情况
     */
    public String shareUsers;

    /**
     * 空间文件数量
     */
    public String filenum;

    /**
     * 空间存储量
     */
    public String storageSize;
}
