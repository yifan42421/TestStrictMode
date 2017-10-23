package com.example.byc.teststrictmode;

import android.os.StrictMode;
import android.os.SystemClock;

/**
 * Created by byc on 2017/10/23.
 * 定义一个耗时任务，当大于500毫秒时，提示用户
 */

public class TaskExecutor {
    private static long SLOW_CALL = 500;

    public void executeTask(Runnable task) {
        long startTime = SystemClock.uptimeMillis();
        task.run();
        long cost = SystemClock.uptimeMillis() - startTime;
        if (cost > SLOW_CALL) {
            StrictMode.noteSlowCall("slowCall cost=" + cost);
        }
    }
}
