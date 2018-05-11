/**
 * @(#)Lock, 2018年05月10日.
 * <p>
 * Copyright 2015 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.mao.lock;

import com.mao.bean.LocalInfo;
import com.mao.bean.LockData;
import com.mao.redis.RedisClient;
import com.mao.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author 王东(hzwangdong @ corp.netease.com)
 * @date 2018/5/10
 */
public class Lock {

    private static final Logger logger = LoggerFactory.getLogger(Lock.class);

    private RedisClient redisClient = RedisClient.getInstance();

    public boolean tryLock(String key, int leaseTime, TimeUnit timeUnit) {
        if (leaseTime <= 0) {
            throw new RuntimeException("lease time mast not be zero.");
        }
        long leaseMillis = timeUnit.toMillis(leaseTime);
        String threadId = genThreadId();
        String value = threadId + "_" + (System.currentTimeMillis() + leaseMillis);
        String dbValue = redisClient.get(key);
        if (!redisClient.exists(key)) {
            return redisClient.setIfNotExist(key, value);
        } else {
            LockData lockData = getLockData(dbValue);
            if (StringUtils.isEmpty(lockData.getThreadId())) {
                 if (redisClient.setIfNotExist(key, value)) {
                     return true;
                 } else {
                     return redisClient.compareAndSet(key, dbValue, value);
                 }
            } else if (threadId.equals(lockData.getThreadId())) {
                return redisClient.compareAndSet(key, dbValue, value);
            } else if (lockData.getLeaseTime() <= System.currentTimeMillis()) {
                return redisClient.compareAndSet(key, dbValue, value);
            } else {
                return false;
            }
        }
    }

    public boolean unLock(String key) {
        if (redisClient.exists(key)) {
            String dbValue = redisClient.get(key);
            String threadId = genThreadId();
            LockData lockData = getLockData(dbValue);
            if (lockData.getThreadId().equals(threadId)) {
                return redisClient.compareAndDelete(key, dbValue);
            }
        }
        return false;
    }

    public boolean isLocked(String key) {
        String value = redisClient.get(key);
        if (value == null || value.length() == 0) {
            return false;
        } else {
            LockData lockData = getLockData(value);
            return lockData.getLeaseTime() > System.currentTimeMillis();
        }
    }

    public boolean forceUnLock(String key) {
        return redisClient.delete(key);
    }

    private String genThreadId() {
        return LocalInfo.getInstance().getPid() + "_" + Thread.currentThread().getId();
    }

    private LockData getLockData(String value) {
        LockData lockData = new LockData();
        if (value == null || value.length() == 0) {
            return lockData;
        }
        int index = value.lastIndexOf('_');
        if (index > 0) {
            lockData.setThreadId(value.substring(0, index));
            if (index < value.length()) {
                lockData.setLeaseTime(Long.parseLong(value.substring(index + 1)));
            }
        }
        return lockData;
    }
}
