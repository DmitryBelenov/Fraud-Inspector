package sys.cache;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.type.AttributeComposition;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AttributeHolder {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final Map<String, AttributeComposition> versions = new HashMap<>();
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static final Lock rLock = rwLock.readLock();
    private static final Lock wLock = rwLock.writeLock();

    public static void init() {
        Iterator<AttributeComposition> acIt = AttributeComposition.getIterator();
        while (acIt.hasNext()) {
            final AttributeComposition ac = acIt.next();
            versions.put(ac.getCode(), ac);
        }
    }
}
