package sys;

import database.DBConnectionHolder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.cache.DBCacheLoader;
import sys.type.StatCollector;
import sys.type.TimeInterval;
import sys.type.TimeMonitor;

import java.sql.SQLException;

public class FISystem {
    public static final Logger log = LogManager.getLogger(FISystem.class);

    public static void load() {
        try {
            log.info("FRAUD INSPECTOR SYSTEM start loading..");
            DBConnectionHolder.init();

            syncLoad();

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

    private static void syncLoad() throws SQLException {
        /* order matters */
        /* 1 */ DBCacheLoader.add(TimeInterval.getDC());
        /* 2 */ DBCacheLoader.add(StatCollector.getDC());
        /* 3 */ DBCacheLoader.add(TimeMonitor.getDC());

        DBCacheLoader.loadAll();
    }
}
