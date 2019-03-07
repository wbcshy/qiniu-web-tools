package com.qiniu.dora.model;

/**
 * 文字水印实体类
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.dora.pfop
 * @email wangbencheng@qiniu.com
 * @date:2019/3/1
 */
public class VmText {

    private String text;
    private String gravity;
    private String font;
    private String fontStyle;
    private String fontColor;
    private String fontSize;
    private String offsetX;
    private String offsetY;
    private String position;
    private String duration;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getGravity() {
        return gravity;
    }

    public void setGravity(String gravity) {
        this.gravity = gravity;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
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
