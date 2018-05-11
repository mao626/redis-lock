/**
 * @(#)LoggerUtils, 2018年05月11日.
 * <p>
 * Copyright 2015 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.mao.util;

import org.slf4j.Logger;

/**
 * @author 王东(hzwangdong @ corp.netease.com)
 * @date 2018/5/11
 */
public class LoggerUtils {

    public static void debug(Logger logger, String message, Object... params) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, params);
        }
    }

    public static void info(Logger logger, String message, Object... params) {
        if (logger.isInfoEnabled()) {
            logger.info(message, params);
        }
    }

    public static void warn(Logger logger, String message, Object... params) {
        if (logger.isWarnEnabled()) {
            logger.warn(message, params);
        }
    }

    public static void error(Logger logger, String message, Object... params) {
        if (logger.isErrorEnabled()) {
            logger.error(message, params);
        }
    }

    public static void error(Logger logger, String message, Exception e, Object... params) {
        if (logger.isErrorEnabled()) {
            logger.error(message, params, e);
        }
    }
}
