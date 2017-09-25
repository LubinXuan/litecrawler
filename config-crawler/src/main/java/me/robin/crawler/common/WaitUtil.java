package me.robin.crawler.common;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Lubin.Xuan on 2017-09-20.
 * {desc}
 */
public class WaitUtil {
    public static void waitObject(final AtomicBoolean waitObject){
        if (waitObject.get()) {
            synchronized (waitObject) {
                try {
                    waitObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
