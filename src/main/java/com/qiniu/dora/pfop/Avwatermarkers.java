package com.qiniu.dora.pfop;

import com.qiniu.dora.params.AvwatermarkersParams;

/**
 * 使用AvWartersMarkers接口
 * 内部参考使用文档：https://github.com/qbox/product/blob/625e2e6a6d599348cf286b4fe9d63e2668f60baf/dora/avwatermarks.md
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.dora.pfop
 * @email wangbencheng@qiniu.com
 * @date:2019/3/1
 */
public class Avwatermarkers {

    /**
     * 拼接添加图片水印命令
     * @param args
     * @return
     */
    public String getPfopStr(AvwatermarkersParams args) {
        String fop = "avwatermarks/";
        StringBuffer fops = new StringBuffer(fop);
        System.out.println(args.getClass().getAnnotations());
        fops = args.getFormat() == null ? fops : fops.append(args.getFormat());
        return fop;
    }

}
