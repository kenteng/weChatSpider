/**
 * Config.java
 * Created by zhusy on 2017/3/21 0021 17:23
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
package com.ifeng.weChatSpider.Util;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

public class Config {
    private static final String configFile = "config.properties";
    private static final ReentrantLock mainLock = new ReentrantLock();
    private static Config me;
    private static Logger logger = Logger.getLogger(Config.class);
    private static Properties properties;

    private Config() {
        init();
    }

    public static Config getMe() {
        mainLock.lock();
        try {
            return me == null ? me = new Config() : me;
        } finally {
            mainLock.unlock();
        }
    }

    public static String getHttpHost(){
        return getMe().get("httphost");
    }

    public static String[] getProxy(){
        return getHttpHost().split(":");
    }

    public static void init(String filePath) {
        mainLock.lock();
        logger.info("init properties");
        InputStream stream = null;
        try {
            if (properties != null) {
                properties.clear();
            } else {
                properties = new Properties();
            }
            stream=Config.class.getClassLoader().getResourceAsStream("config.properties");
            BufferedReader bf = new BufferedReader(new InputStreamReader(stream));

            properties.load(bf);

        } catch (FileNotFoundException e) {
            logger.error("properties file not found!!!　", e);

            System.exit(-1);
        } catch (IOException e) {
            logger.error(e);
        } finally {
            mainLock.unlock();
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void init() {
        init(configFile);
    }

    public void put(String key, String val) {
        properties.put(key, val);
    }

    public Properties getAll() {
        return properties;
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}
