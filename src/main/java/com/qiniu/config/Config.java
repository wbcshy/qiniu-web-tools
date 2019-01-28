package com.qiniu.config;

import java.io.*;
import java.util.Properties;

/**
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu
 * @email wangbencheng@qiniu.com
 * @date:2019/1/25
 */
public class Config {

    private Properties properties;

    public Config(String path) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
            properties = new Properties();
            properties.load(new InputStreamReader(new BufferedInputStream(inputStream), "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取配置信息名称
     * @param key
     * @return
     */
    public String getProperty(String key) {
        if (this.properties.containsKey(key)) {
            return this.properties.getProperty(key);
        }else {
            try {
                throw new IOException("this param not exist!");
            } catch (IOException e) {
                return e.getMessage();
            }
        }
    }

}
