package com.qiniu.dora.model;

/**
 * 图片水印实体类
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.dora.pfop
 * @email wangbencheng@qiniu.com
 * @date:2019/3/1
 */
public class VmImage {

    private String imageUrl;
    private String gravity;
    private String scale;
    private String scaleType;
    private String offsetX;
    private String offsetY;
    private String position;
    private String duration;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGravity() {
        return gravity;
    }

    public void setGravity(String gravity) {
        this.gravity = gravity;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public String getScaleType() {
        return scaleType;
    }

    public void setScaleType(String scaleType) {
        this.scaleType = scaleType;
    }

    public String getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(String offsetX) {
        this.offsetX = offsetX;
    }

    public String getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(String offsetY) {
        this.offsetY = offsetY;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
