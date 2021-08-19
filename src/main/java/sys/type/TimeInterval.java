package sys.type;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.cache.CacheUnit;
import sys.cache.DBCache;
import utils.StringUtils;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.stream.Stream;

public class TimeInterval implements CacheUnit, IDBType {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final DCTInterval DC_T_INTERVAL = new DCTInterval();

    private static final String TABLE_NAME = "time_intervals";
    private static final String[] FIELDS = new String[]{"ID", "CODE", "DESCRIPTION", "INTERVAL_MS", "LAST_ACTIVITY_DT_TM", "ACTIVITY"};
    private static final String[] SQL_FIELDS = new String[FIELDS.length - 3];
    static {
        System.arraycopy(FIELDS, 1, SQL_FIELDS, 0, 3);
    }

    private final Long id;

    private final String code;

    private final String description;

    private final Long intervalMs;

    private final Date lastActivityDTm;

    private final Activity activity;

    public TimeInterval(Long id, String code, String description, Long intervalMs, Date lastActivityDTm, Activity activity) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.intervalMs = intervalMs;
        this.lastActivityDTm = lastActivityDTm;
        this.activity = activity;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Long getIntervalMs() {
        return intervalMs;
    }

    public Date getLastActivityDTm() {
        return lastActivityDTm;
    }

    public Activity getActivity() {
        return activity;
    }

    public boolean readyForCleanStats(final ConsumerRecord<?, StatTransactionData> record) {
        return System.currentTimeMillis() - record.timestamp() > intervalMs;
    }

    public static DCTInterval getDC() {
        return DC_T_INTERVAL;
    }

    public static TimeInterval get(Long id) {
        return DC_T_INTERVAL.get(id);
    }

    public static TimeInterval getByCode(String code) {
        return DC_T_INTERVAL.getByCode(code);
    }

    public static TimeInterval add(Long id, TimeInterval tm) {
        return DC_T_INTERVAL.addByKey(id, tm);
    }

    public static Stream<TimeInterval> getStream() {
        return DC_T_INTERVAL.cacheVStream();
    }

    public static Iterator<TimeInterval> getIterator() {
        return DC_T_INTERVAL.cacheVIterator();
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String[] getFields() {
        return SQL_FIELDS;
    }

    @Override
    public String getValuesString() {
        return StringUtils.getSQLValues(
                code,
                description,
                intervalMs
        );
    }

    private static class DCTInterval extends DBCache<TimeInterval> {

        public DCTInterval() {
            super();
        }

        @Override
        protected Iterator<TimeInterval> cacheVIterator() {
            return tpCache.values().iterator();
        }

        @Override
        protected Stream<TimeInterval> cacheVStream() {
            return tpCache.values().stream();
        }

        @Override
        protected TimeInterval addByKey(Long key, TimeInterval type) {
            return tpCache.put(key, type);
        }

        @Override
        protected TimeInterval addByCode(String code, TimeInterval type) { return codeCache.put(code, type); }

        @Override
        protected TimeInterval get(Long key) {
            return tpCache.get(key);
        }

        @Override
        protected TimeInterval getByCode(String code) {
            return codeCache.get(code);
        }

        @Override
        protected int cacheSize() {
            return tpCache.size();
        }

        @Override
        public void load() throws SQLException {
            final String sqlAll = "SELECT %s,%s,%s,%s,%s,%s FROM " + TABLE_NAME +" WHERE ACTIVITY = 'A'";
            try (Connection conn = getDbSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement(String.format(sqlAll, (Object[]) FIELDS))) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final Long id = rs.getLong(1);                 // ID
                        final String code = rs.getString(2);           // CODE

                        TimeInterval ti = new TimeInterval(
                                id,
                                code,
                                rs.getString(3),                        // DESCRIPTION
                                rs.getLong(4),                          // INTERVAL_MS
                                rs.getDate(5),                          // LAST_ACTIVITY_DT_TM
                                Activity.valueOf(rs.getString(6))       // ACTIVITY
                        );

                        addByKey(id, ti);
                        addByCode(code, ti);
                    }
                    log.info("Time Intervals cached, ttl=" + cacheSize());
                }
            }
        }
    }
}
