package com.meraj.rxjava_demo;

import android.util.Log;

/**
 * Created by meraj on 18/09/16.
 */
public class Utils {
    public static String getThreadName () {
        Thread t = Thread.currentThread();
        return t.getName();
    }
}
