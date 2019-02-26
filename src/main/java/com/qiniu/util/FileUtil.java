package com.qiniu.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author: wangbencheng
 * @version: v1.0
 * @description: com.qiniu.utils
 * @email wangbencheng@qiniu.com
 * @date:2019/2/24
 */
public class FileUtil {

    /**
     * 读取properties配置文件
     * @param fileName
     * @return
     */
    public static Properties getPropertiesInfo(String fileName) {
        InputStream inputStream = FileUtil.class.getClassLoader().getResourceAsStream(fileName);
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(new BufferedInputStream(inputStream), "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }

    /**
     * 根据key获取配置信息名称
     * @param properties
     * @param key
     * @return
     */
    public static String getProperty(Properties properties, String key) {
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }else {
            try {
                throw new IOException("this param not exist!");
            } catch (IOException e) {
                return e.getMessage();
            }
        }
    }

}
