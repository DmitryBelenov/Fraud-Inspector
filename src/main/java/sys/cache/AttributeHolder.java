package sys.cache;

import attribute.BaseAttributes;
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

    private static final int SIZE = BaseAttributes.size();

    public static void init() {
        Iterator<AttributeComposition> acIt = AttributeComposition.getIterator();
        while (acIt.hasNext()) {
            final AttributeComposition ac = acIt.next();
            versions.put(ac.getCode(), ac);
        }
        log.info("Attribute holder get " + versions.size() + " compositions");
    }

    public static AttributeComposition getByCode(final String acCode) {
        rLock.lock();
        try {
            return versions.get(acCode);
        } finally {
            rLock.unlock();
        }
    }

    public static String[] getIndexedAttrValues(Map<String, String> pmAttrMap, final String code) throws Exception {
        final AttributeComposition ac = getByCode(code);
        if (ac == null)
            throw new Exception("Attribute composition by code '" + code + "' not found");

        final String[] fieldValues = new String[SIZE];
        for (Map.Entry<String, String> pmE : pmAttrMap.entrySet()) {
            int idx = ac.getAttributeIdx(pmE.getKey());
            if (idx != -1) {
                fieldValues[idx] = pmE.getValue();
            } else throw new Exception("Attribute '" + pmE.getKey() + "' unavailable in composition " + code);
        }
        return fieldValues;
    }
}
