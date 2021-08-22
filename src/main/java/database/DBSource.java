package database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.type.IDBType;
import utils.SysUtils;

import java.beans.PropertyVetoException;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.*;

public abstract class DBSource implements IDBConnection {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private final ComboPooledDataSource cPoolDS;
    protected final ExecutorService dbSrv = SysUtils.newFixedThreadPool("db.source", Runtime.getRuntime().availableProcessors());

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

    public abstract void insert(IDBType type);

    public abstract void update(IDBType type, String valWhere);

    public ResultSet callableSQL(String sql) {
        Callable<ResultSet> callTask = ()-> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                 return ps.executeQuery();
            } catch (SQLException sqlEx) {
                log.error("Query execute error, sql: " + sql, sqlEx);
            }
            return null;
        };

        Future<ResultSet> resFt = dbSrv.submit(callTask);
        ResultSet rs = null;
        try {
            rs = resFt.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e);
        }

        return rs;
    }
}
