package sys;

import database.DBConnectionHolder;
import kafka.PaymentLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.cache.AttributeHolder;
import sys.cache.DBCacheLoader;
import sys.type.AttributeComposition;
import sys.type.StatCollector;
import sys.type.TimeInterval;
import sys.type.TimeMonitor;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class FISystem {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static final List<ExecutorService> EX_SRV_LS = new ArrayList<>();

    public static void load() {
        try {
            log.info("FRAUD INSPECTOR SYSTEM start loading..");
            DBConnectionHolder.init();

            syncLoadDBCaches();
            AttributeHolder.init();

            PaymentLoader.start();

            log.info("\n\n        *****************************************************\n" +
                      "                       FI SYSTEM LOADED SUCCESSFULLY           \n" +
                      "        *****************************************************");
        } catch (Exception e) {
            log.error(e);
            log.error("\n\n        ****************************************************\n" +
                        "                       FI SYSTEM LOADING FAULT              \n" +
                        "        ****************************************************");
        }
    }

    private static void syncLoadDBCaches() throws SQLException {
        /* order matters */
        /* 1 */ DBCacheLoader.add(AttributeComposition.getDC());
        /* 2 */ DBCacheLoader.add(TimeInterval.getDC());
        /* 3 */ DBCacheLoader.add(StatCollector.getDC());
        /* 4 */ DBCacheLoader.add(TimeMonitor.getDC());

        DBCacheLoader.loadAll();
    }

    public static void registerExecutorService(final ExecutorService es) {
        EX_SRV_LS.add(es);
    }

    public static void stopAllExecs() {
        EX_SRV_LS.forEach(es -> {
            es.shutdown();
            try {
                boolean esTerminated = es.awaitTermination(2, TimeUnit.SECONDS);
                if (!esTerminated)
                    log.warn("Executor service was not terminated by timeout");
            } catch (InterruptedException e) {
                es.shutdownNow();
            }
        });
    }
}
