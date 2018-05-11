/**
 * @(#)Main, 2018年05月11日.
 * <p>
 * Copyright 2015 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.mao;

import com.mao.lock.Lock;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * @author 王东(hzwangdong @ corp.netease.com)
 * @date 2018/5/11
 */
public class Main {

    private static String testLockName = "testLock";

    public static void main(String[] args) {
        testLock();
    }

    private static void testLock() {
        final Lock lock = new Lock();
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(100);

        Runnable runnable = new Runnable() {
            public void run() {
                System.out.println("thread id=" + Thread.currentThread().getId() + " start. " + System.currentTimeMillis());
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("thread id=" + Thread.currentThread().getId() + " run. " + System.currentTimeMillis());
                if (lock.tryLock(testLockName, 100, TimeUnit.SECONDS)) {
                    System.out.println("i get lock, thread id=" + Thread.currentThread().getId());
                }
            }
        };

        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(runnable);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread.start();
        }
    }

}
