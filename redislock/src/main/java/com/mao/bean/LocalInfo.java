/**
 * @(#)LocalInfo, 2018年05月11日.
 * <p>
 * Copyright 2015 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.mao.bean;

import java.lang.management.ManagementFactory;

/**
 * @author 王东(hzwangdong @ corp.netease.com)
 * @date 2018/5/11
 */
public class LocalInfo {

    private volatile static LocalInfo localInfo;

    private LocalInfo (String pid) {
        this.pid = pid;
    }

    private String pid;

    public String getPid() {
        return pid;
    }

    public static LocalInfo getInstance() {
        if (localInfo == null) {
            synchronized (LocalInfo.class) {
                if (localInfo == null) {
                    localInfo = new LocalInfo(ManagementFactory.getRuntimeMXBean().getName());
                }
            }
        }
        return localInfo;
    }
}
