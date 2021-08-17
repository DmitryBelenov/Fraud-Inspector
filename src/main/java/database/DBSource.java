package database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.beans.PropertyVetoException;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public abstract class DBSource implements IDBConnection {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private final ComboPooledDataSource cPoolDS;

    public DBSource(final Properties prop) {
        cPoolDS = new ComboPooledDataSource();
        try {
            cPoolDS.setDriverClass(prop.getProperty("drvClass"));
        } catch (PropertyVetoException e) {
            log.error("Error to set db driver class to connection pool", e);
        }
        cPoolDS.setJdbcUrl(prop.getProperty("url"));
        cPoolDS.setUser(prop.getProperty("user"));
        cPoolDS.setPassword(prop.getProperty("password"));

        // set options
        cPoolDS.setMaxStatements             (180);
        cPoolDS.setMaxStatementsPerConnection(180);
        cPoolDS.setMinPoolSize               ( 50);
        cPoolDS.setAcquireIncrement          ( 10);
        cPoolDS.setMaxPoolSize               ( 60);
        cPoolDS.setMaxIdleTime               ( 30);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return cPoolDS.getConnection();
    }
}
