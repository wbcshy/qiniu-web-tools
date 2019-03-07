package com.qiniu.dora.params;

import com.qiniu.dora.model.VmImage;
import com.qiniu.dora.model.VmText;

import java.util.List;

/**
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.dora.model
 * @email wangbencheng@qiniu.com
 * @date:2019/3/1
 */
public class AvwatermarkersParams {

    private String format;
    private String frameRate;
    private String SubtitleURL;
    private String BitRate;
    private String AudioQuality;
    private String SamplingRate;
    private String ChannelNum;
    private String Aspect;
    private String Resolution;
    private String Start;
    private String Duration;
    private String stripmeta;
    private String wmShortest;
    private String vmIgnoreLoop;

    private List<VmImage> imageList;
    private List<VmText> textList;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(String frameRate) {
        this.frameRate = frameRate;
    }

    public String getSubtitleURL() {
        return SubtitleURL;
    }

    public void setSubtitleURL(String subtitleURL) {
        SubtitleURL = subtitleURL;
    }

    public String getBitRate() {
        return BitRate;
    }

    public void setBitRate(String bitRate) {
        BitRate = bitRate;
    }

    public String getAudioQuality() {
        return AudioQuality;
    }

    public void setAudioQuality(String audioQuality) {
        AudioQuality = audioQuality;
    }

    public String getSamplingRate() {
        return SamplingRate;
    }

    public void setSamplingRate(String samplingRate) {
        SamplingRate = samplingRate;
    }

    public String getChannelNum() {
        return ChannelNum;
    }

    public void setChannelNum(String channelNum) {
        ChannelNum = channelNum;
    }

    public String getAspect() {
        return Aspect;
    }

    public void setAspect(String aspect) {
        Aspect = aspect;
    }

    public String getResolution() {
        return Resolution;
    }

    public void setResolution(String resolution) {
        Resolution = resolution;
    }

    public String getStart() {
        return Start;
    }

    public void setStart(String start) {
        Start = start;
    }

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }

    public String getStripmeta() {
        return stripmeta;
    }

    public void setStripmeta(String stripmeta) {
        this.stripmeta = stripmeta;
    }

    public String getWmShortest() {
        return wmShortest;
    }

    public void setWmShortest(String wmShortest) {
        this.wmShortest = wmShortest;
    }

    public String getVmIgnoreLoop() {
        return vmIgnoreLoop;
    }

    public void setVmIgnoreLoop(String vmIgnoreLoop) {
        this.vmIgnoreLoop = vmIgnoreLoop;
    }

    public List<VmImage> getImageList() {
        return imageList;
    }

    public void setImageList(List<VmImage> imageList) {
        this.imageList = imageList;
    }

    public List<VmText> getTextList() {
        return textList;
    }

    public void setTextList(List<VmText> textList) {
        this.textList = textList;
    }
}
