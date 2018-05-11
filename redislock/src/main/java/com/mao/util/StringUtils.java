/**
 * @(#)StringUtils, 2018年05月11日.
 * <p>
 * Copyright 2015 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.mao.util;

/**
 * @author 王东(hzwangdong @ corp.netease.com)
 * @date 2018/5/11
 */
public class StringUtils {

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
}
