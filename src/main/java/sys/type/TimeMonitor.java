package sys.type;

import kafka.SlidingWindow;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.cache.CacheUnit;
import sys.cache.DBCache;
import sys.key.FIKeyCmp;
import sys.stat.StatisticsProcessor;
import sys.stat.TimeStatistics;
import utils.StringUtils;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class TimeMonitor implements CacheUnit, IDBType {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static final DCTMonitor DC_T_MONITOR = new DCTMonitor();

    private static final String TABLE_NAME = "time_monitors";
    private static final String[] FIELDS = new String[]{"ID", "CODE", "DESCRIPTION", "INTERVAL_ID", "COLLECTOR_ID", "LAST_ACTIVITY_DT_TM", "ACTIVITY"};
    private static final String[] SQL_FIELDS = new String[FIELDS.length - 3];
    static {
        System.arraycopy(FIELDS, 1, SQL_FIELDS, 0, 4);
    }

    private final Long id;

    private final String code;

    private final String description;

    private final TimeInterval interval;

    private final StatCollector collector;

    private final Date lastActivityDTm;

    private final Activity activity;

    private final SlidingWindow swStatCleaner;

    private final Map<FIKeyCmp, TimeStatistics> statistics;

    public TimeMonitor(Long id, String code, String description, TimeInterval interval, StatCollector collector, Date lastActivityDTm, Activity activity) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.interval = interval;
        this.collector = collector;
        this.lastActivityDTm = lastActivityDTm;
        this.activity = activity;

        swStatCleaner = new SlidingWindow(getCode(), interval, this::removeFromStatistics);
        statistics = new HashMap<>();
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

    public TimeInterval getInterval() {
        return interval;
    }

    public StatCollector getCollector() {
        return collector;
    }

    public Map<FIKeyCmp, TimeStatistics> getStatistics() {
        return statistics;
    }

    public Date getLastActivityDTm() {
        return lastActivityDTm;
    }

    public Activity getActivity() {
        return activity;
    }

    public void addToStatistics(final StatTransactionData data) {
        if (collector.filter(data)) {
            StatisticsProcessor.addToStatistics(this, data);
        }
    }

    private void removeFromStatistics(final StatTransactionData data) {
        if (collector.filter(data)) {
            StatisticsProcessor.removeFromStatistics(this, data);
        }
    }

    public static DCTMonitor getDC() {
        return DC_T_MONITOR;
    }

    public static TimeMonitor get(Long id) {
        return DC_T_MONITOR.get(id);
    }

    public static TimeMonitor getByCode(String code) {
        return DC_T_MONITOR.getByCode(code);
    }

    public static TimeMonitor add(Long id, TimeMonitor tm) {
        return DC_T_MONITOR.addByKey(id, tm);
    }

    public static Stream<TimeMonitor> getStream() {
        return DC_T_MONITOR.cacheVStream();
    }

    public static Iterator<TimeMonitor> getIterator() {
        return DC_T_MONITOR.cacheVIterator();
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
                interval.getId(),
                collector.getId()
        );
    }

    private static class DCTMonitor extends DBCache<TimeMonitor> {
        public DCTMonitor() {
            super();
        }

        @Override
        protected Iterator<TimeMonitor> cacheVIterator() {
            return tpCache.values().iterator();
        }

        @Override
        protected Stream<TimeMonitor> cacheVStream() {
            return tpCache.values().stream();
        }

        @Override
        protected TimeMonitor addByKey(Long key, TimeMonitor type) {
            return tpCache.put(key, type);
        }

        @Override
        protected TimeMonitor addByCode(String code, TimeMonitor type) { return codeCache.put(code, type); }

        @Override
        protected TimeMonitor get(Long key) {
            return tpCache.get(key);
        }

        @Override
        protected TimeMonitor getByCode(String code) {
            return codeCache.get(code);
        }

        @Override
        protected int cacheSize() {
            return tpCache.size();
        }

        @Override
        public void load() throws SQLException {
            final String sqlAll = "SELECT %s,%s,%s,%s,%s,%s,%s FROM " + TABLE_NAME +" WHERE ACTIVITY = 'A'";
            try (Connection conn = getDbSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement(String.format(sqlAll, (Object[]) FIELDS))) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final Long id = rs.getLong(1);                  // ID
                        final String code = rs.getString(2);            // CODE

                        Long collId = rs.getLong(5);                    // COLLECTOR_ID
                        final StatCollector sc = StatCollector.get(collId);
                        if (sc == null) {
                            log.error("StatCollector not found for TimeMonitor, id:" + id + " code:" + code + "");
                        }
                        Long tmIntId = rs.getLong(4);                   // INTERVAL_ID
                        final TimeInterval ti = TimeInterval.get(tmIntId);
                        if (ti == null) {
                            log.error("TimeInterval not found for TimeMonitor, id:" + id + " code:" + code + "");
                        }
                        TimeMonitor tm = new TimeMonitor(
                                id,
                                code,
                                rs.getString(3),                        // DESCRIPTION
                                ti,                                                // time interval
                                sc,                                                // stat collector
                                rs.getDate(6),                          // LAST_ACTIVITY_DT_TM
                                Activity.valueOf(rs.getString(7))       // ACTIVITY
                        );

                        addByKey(id, tm);
                        addByCode(code, tm);
                    }
                    log.info("Time Monitors cached, ttl=" + cacheSize());
                }
            }
        }
    }
}
