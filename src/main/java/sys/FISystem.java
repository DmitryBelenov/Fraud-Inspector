package sys;

import database.DBConnectionHolder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.cache.DBCacheLoader;
import sys.type.AttributeComposition;
import sys.type.StatCollector;
import sys.type.TimeInterval;
import sys.type.TimeMonitor;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;

public class FISystem {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static void load() {
        try {
            log.info("FRAUD INSPECTOR SYSTEM start loading..");
            DBConnectionHolder.init();

            syncLoadDBCaches();

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
}
