package service;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.AsyncContext;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpProcessor {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static final Map<Long, AsyncContext> ASYNC_CTX_CACHE = new ConcurrentHashMap<>();
}
