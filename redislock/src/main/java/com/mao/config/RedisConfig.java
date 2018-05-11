/**
 * @(#)RedisConfig, 2018年05月11日.
 * <p>
 * Copyright 2015 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.mao.config;

import com.mao.redis.RedisClient;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @author 王东(hzwangdong @ corp.netease.com)
 * @date 2018/5/11
 */
public class RedisConfig {

    private volatile static RedisConfig redisConfig;

    private String server = "127.0.0.1";

    private int maxTotal = 100;

    private int maxIdle = 10;

    private int minIdle = 0;

    private int port = 6379;

    private int timeout = 5000;

    private RedisConfig() {
        init();
    }

    private void init() {
        URL url = RedisClient.class.getClassLoader().getResource("config/redis.properties");
        Properties properties = new Properties();
        try {
            properties.load(url.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.server = properties.getProperty("redis.server", "127.0.0.1");
        this.port = Integer.parseInt(properties.getProperty("redis.port", "6379"));
        this.timeout = Integer.parseInt(properties.getProperty("redis.timeout", "2000"));
        this.maxTotal = Integer.parseInt(properties.getProperty("redis.maxTotal", "10"));
        this.maxIdle = Integer.parseInt(properties.getProperty("redis.maxIdle", "10"));
        this.minIdle = Integer.parseInt(properties.getProperty("redis.minIdle", "10"));

    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public static RedisConfig getInstance() {
        if (redisConfig == null) {
            synchronized (RedisConfig.class) {
                if (redisConfig == null) {
                    redisConfig = new RedisConfig();
                }
            }
        }
        return redisConfig;
    }


}
