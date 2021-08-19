package utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.FISystem;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SysUtils {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static ExecutorService newFixedThreadPool(final String name, final int size) {
        final ExecutorService s = Executors.newFixedThreadPool(size,
                new ThreadFactory() {
                    private final AtomicInteger num = new AtomicInteger();

                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                        thread.setDaemon(true);
                        thread.setName(name + "-" + num.getAndAdd(1) + "/" + size);
                        return thread;
                    }
                });
        FISystem.registerExecutorService(s);
        return s;
    }
}
