package kafka;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class BootstrapStatChecker {
    private static final ReentrantReadWriteLock rwL = new ReentrantReadWriteLock();
    private static final ReadLock rL = rwL.readLock();
    private static final WriteLock wL = rwL.writeLock();

    private static boolean loaded;

    public static boolean isLoaded() {
        rL.lock();
        try {
            return loaded;
        } finally {
            rL.unlock();
        }
    }

    public static void setLoaded() {
        wL.lock();
        try {
            loaded = true;
        } finally {
            wL.unlock();
        }
    }
}
