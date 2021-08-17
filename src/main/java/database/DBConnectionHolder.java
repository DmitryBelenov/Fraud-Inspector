package database;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.FIParams;

import java.lang.invoke.MethodHandles;
import java.util.Properties;

public class DBConnectionHolder {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static DBConnectionHolder dbCHolder;
    private final DBSource dbSource;

    private DBConnectionHolder() throws Exception {
        if (FIParams.DB_TYPE == null) {
            throw new Exception("Database type not set, check config file");
        }

        DatabaseType type = DatabaseType.valueOf(FIParams.DB_TYPE);
        log.info("DBType: " + type);

        dbSource = dbSourceFactory(type);
        if (dbSource == null) {
            throw new Exception("Database not initialized");
        }
        log.info("Database initialized successfully");
    }

    private DBSource dbSourceFactory(final DatabaseType type) throws Exception {
        String drvClass;
        Properties props = new Properties();
        props.setProperty("url",FIParams.URL);
        props.setProperty("user",FIParams.USER);
        props.setProperty("password",FIParams.PASSWORD);

        switch (type) {
            case PostgreSQL:
                drvClass = "org.postgresql.Driver";
                props.setProperty("drvClass", drvClass);
                return new DBPostgreSQL(props, drvClass);
            case Oracle:
            case MySQL:
        }
        return null;
    }

    public static void init() throws Exception {
        if (dbCHolder == null) {
            dbCHolder = new DBConnectionHolder();
        }
    }

    public static DBConnectionHolder instance() {
        if (dbCHolder == null) {
            try {
                init();
            } catch (Exception e) {
                log.error("Unexpected error: " + e);
            }
        }
        return dbCHolder;
    }

    public DBSource getSource() {
        return dbSource;
    }
}
