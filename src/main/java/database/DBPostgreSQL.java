package database;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Properties;

public class DBPostgreSQL extends DBSource {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public DBPostgreSQL(Properties prop, String drvClass) throws Exception {
        super(prop);
        // check postgres jdbc driver
        try {
            Class.forName(drvClass);
        } catch (ClassNotFoundException e) {
            log.error("Postgres JDBC driver not found: ", e);
            throw new Exception(e);
        }
    }
}
