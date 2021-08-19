package database;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.type.IDBType;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DBPostgreSQL extends DBSource {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final String INSERT = "INSERT INTO %s (%s) VALUES (%s)";
    private static final String UPDATE = "UPDATE %s SET %s";

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

    @Override
    void insert(IDBType type) {
        dbSrv.execute(() -> {
            final String fields = String.join(",", type.getFields());
            final String sql = String.format(INSERT, type.getTableName(), fields, type.getValuesString());
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            } catch (SQLException sqlEx) {
                log.error("Insert type " + type.getClass().getName() + " error, sql: " + sql, sqlEx);
            }
        });
    }

    @Override
    void update(IDBType type, String where) {
        dbSrv.execute(() -> {
            final String sql = String.format(UPDATE, type.getTableName(), where);
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                 ps.executeUpdate();
            } catch (SQLException sqlEx) {
                log.error("Update type " + type.getClass().getName() + " error, sql: " + sql, sqlEx);
            }
        });
    }
}
