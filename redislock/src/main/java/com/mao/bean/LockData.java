/**
 * @(#)LockValue, 2018年05月10日.
 * <p>
 * Copyright 2015 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.mao.bean;

/**
 * @author 王东(hzwangdong @ corp.netease.com)
 * @date 2018/5/10
 */
public class LockData {

    private String threadId = "";// 锁的线程

    private long leaseTime;// 锁过期时间戳，单位毫秒

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public long getLeaseTime() {
        return leaseTime;
    }

    public void setLeaseTime(long leaseTime) {
        this.leaseTime = leaseTime;
    }
}
