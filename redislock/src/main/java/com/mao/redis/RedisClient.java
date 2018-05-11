/**
 * @(#)RedisClient, 2018年05月10日.
 * <p>
 * Copyright 2015 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.mao.redis;

import com.alibaba.fastjson.JSON;
import com.mao.config.RedisConfig;
import com.mao.lock.Lock;
import com.mao.util.LoggerUtils;
import com.mao.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 王东(hzwangdong @ corp.netease.com)
 * @date 2018/5/10
 */
public class RedisClient {

    private static final Logger logger = LoggerFactory.getLogger(Lock.class);

    private volatile static RedisClient client;

    /**
     * 初始化方法
     */
    private void init() {
        try {
            RedisConfig redisConfig = RedisConfig.getInstance();
            int timeout = redisConfig.getTimeout();
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(redisConfig.getMaxTotal());
            config.setMaxIdle(redisConfig.getMaxIdle());
            config.setMinIdle(redisConfig.getMinIdle());
            String server = redisConfig.getServer();
            int port = redisConfig.getPort();
            pool = new JedisPool(config, server, port, timeout);
            LoggerUtils.info(logger, "success init redis client. redis config={}", JSON.toJSONString(redisConfig));
        } catch (Exception e) {
            LoggerUtils.error(logger, "error happens when init redis client.", e);
        }
    }

    /**
     * 私有构造方法，实现单例
     */
    private RedisClient() {
        init();
        loadScripts();
    }

    /**
     * redis连接池对象
     */
    private Pool<Jedis> pool;

    /**
     * 存储lua脚本
     */
    private final ConcurrentHashMap<String, String> scriptFileShaMap = new ConcurrentHashMap<String, String>();

    /**
     * 根据key获取value
     * @param key
     * @return
     */
    public String get(String key) {
        Jedis jedis = this.pool.getResource();
        try {
            String result = jedis.get(key);
            LoggerUtils.debug(logger, "op:get, key={}, value={}", key, result);
            return result;
        } catch (JedisConnectionException e) {
            LoggerUtils.error(logger, "op:get error, key={}", e, key);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
                jedis = null;
            }
        }
    }

    /**
     * 如果redis中不存在key，则存储key-value
     * @param key
     * @param value
     * @return
     */
    public boolean setIfNotExist(String key, String value) {
        Jedis jedis = this.pool.getResource();
        try {
            long result = jedis.setnx(key, value);
            LoggerUtils.debug(logger, "op:setIfNotExist, key={}, value={}", key, result);
            return result == 1;
        } catch (JedisConnectionException e) {
            LoggerUtils.error(logger, "op:setIfNotExist error, key={}", e, key);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
                jedis = null;
            }
        }
    }

    /**
     * 基于lua脚本实现cas
     * @param key key
     * @param oldValue 旧值
     * @param newValue 新值
     * @return
     */
    public boolean compareAndSet(String key, String oldValue, String newValue) {
        LoggerUtils.info(logger, "op: compare and set. key:{}, oldValue:{}, newValue:{}," +
                        "cas.lua:{}",
                key, oldValue, newValue,scriptFileShaMap.get("cas.lua"));
        Object value = evalsha(scriptFileShaMap.get("cas.lua"), 3, key, oldValue, newValue);
        if (value == null) {
            return false;
        } else {
            long integer = (Long)value;
            return integer != 0;
        }
    }

    /**
     * 基于lua脚本实现比较并删除的原子操作
     * @param key key
     * @param oldValue 旧值
     * @return
     */
    public boolean compareAndDelete(String key, String oldValue) {
        Object value = evalsha(scriptFileShaMap.get("cad.lua"), 2, key, oldValue);
        if (value == null) {
            return false;
        } else {
            long integer = (Long)value;
            return integer != 0;
        }
    }

    /**
     * 从redis中删除key
     * @param key
     * @return
     */
    public boolean delete(String key) {
        Jedis jedis = this.pool.getResource();
        try {
            long result = jedis.del(key);
            LoggerUtils.debug(logger, "op:delete, key={}, value={}", key, result);
            return result > 0;
        } catch (JedisConnectionException e) {
            LoggerUtils.error(logger, "op:delete error, key={}", e, key);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
                jedis = null;
            }
        }
    }

    /**
     * 判断redis中是否存在key
     * @param key
     * @return
     */
    public boolean exists(String key) {
        Jedis jedis = this.pool.getResource();
        try {
            if (jedis == null) {
                return false;
            }
            Boolean isExist = jedis.exists(key);
            LoggerUtils.debug(logger, "op:exists, key={}, value={}", key, isExist);
            return isExist;
        } catch (JedisConnectionException e) {
            LoggerUtils.error(logger, "op:exists error, key={}", e, key);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
                jedis = null;
            }
        }
    }

    /**
     * 执行lua脚本
     * @param sha1
     * @param keyCount 参数数量
     * @param params 参数列表
     * @return
     */
    private Object evalsha(String sha1, int keyCount, String... params) {
        Jedis jedis = this.pool.getResource();
        try {
            if (jedis == null) {
                return null;
            }
            return jedis.evalsha(sha1, keyCount, params);
        } finally {
            if (jedis != null) {
                jedis.close();
                jedis = null;
            }
        }
    }

    /**
     * 载入脚本到redis
     */
    private void loadScripts() {
        URL url = RedisClient.class.getClassLoader().getResource("script");
        File dir = new File(url.getFile());
        Collection<File> files = FileUtils.listFiles(dir, new String[] {"lua"}, true);
        if (files.size() > 0) {
            for (File file: files) {
                try {
                    String scriptContent = FileUtils.readFileToString(file, "utf-8");
                    if (StringUtils.isEmpty(scriptContent)) {
                        continue;
                    }
                    String scriptSha = scriptLoad(scriptContent);
                    if (!StringUtils.isEmpty(scriptSha)) {
                        scriptFileShaMap.put(file.getName(), scriptSha);
                    }
                } catch (IOException e) {
                    LoggerUtils.error(logger, "read lua file error, url={}", e, url);
                }
            }
        }
    }

    /**
     * 载入脚本
     * @param script 脚本字符串
     * @return
     */
    private String scriptLoad(String script) {
        Jedis jedis = this.pool.getResource();
        try {
            String value = jedis.scriptLoad(script);
            LoggerUtils.info(logger, "op:scriptLoad, script:{}, value:{}", script, value);
            return value;
        } finally {
            if (jedis != null) {
                jedis.close();
                jedis = null;
            }
        }
    }

    /**
     * 实现单例模式
     * @return
     */
    public static RedisClient getInstance() {
        if (client == null) {
            synchronized (RedisClient.class) {
                if (client == null) {
                    client = new RedisClient();
                }
            }
        }
        return client;
    }

}
