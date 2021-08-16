package sys.type;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.cache.CacheUnit;
import sys.cache.DBCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.stream.Stream;

public class StatCollector implements CacheUnit {
    public static final Logger log = LogManager.getLogger(StatCollector.class);

    private static final DCStatCollector DC_STAT_COLLECTOR = new DCStatCollector();

    private static final String TABLE_NAME = "stat_collectors";
    private static final String[] FIELDS = new String[]{"ID", "CODE", "GROUP_BY", "FILTER", "LAST_ACTIVITY_DT_TM", "ACTIVITY"};

    private final Long id;

    private final String code;

    private final String groupBy;

    private final String filter;

    private final Date lastActivityDTm;

    private final Activity activity;

    public StatCollector(Long id, String code, String groupBy, String filter, Date lastActivityDTm, Activity activity) {
        this.id = id;
        this.code = code;
        this.groupBy = groupBy;
        this.filter = filter;
        this.lastActivityDTm = lastActivityDTm;
        this.activity = activity;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public String getFilter() {
        return filter;
    }

    public Date getLastActivityDTm() {
        return lastActivityDTm;
    }

    public Activity getActivity() {
        return activity;
    }

    public static DCStatCollector getDC() {
        return DC_STAT_COLLECTOR;
    }

    public static StatCollector get(Long id) {
        return DC_STAT_COLLECTOR.get(id);
    }

    public static StatCollector getByCode(String code) {
        return DC_STAT_COLLECTOR.getByCode(code);
    }

    public static StatCollector add(Long id, StatCollector tm) {
        return DC_STAT_COLLECTOR.addByKey(id, tm);
    }

    public static Stream<StatCollector> getStream() {
        return DC_STAT_COLLECTOR.cacheVStream();
    }

    public static Iterator<StatCollector> getIterator() {
        return DC_STAT_COLLECTOR.cacheVIterator();
    }

    private static class DCStatCollector extends DBCache<StatCollector> {

        public DCStatCollector() {
            super();
        }

        @Override
        protected Iterator<StatCollector> cacheVIterator() {
            return tpCache.values().iterator();
        }

        @Override
        protected Stream<StatCollector> cacheVStream() {
            return tpCache.values().stream();
        }

        @Override
        protected StatCollector addByKey(Long key, StatCollector type) {
            return tpCache.put(key, type);
        }

        @Override
        protected StatCollector addByCode(String code, StatCollector type) {
            return codeCache.put(code, type);
        }

        @Override
        protected StatCollector get(Long key) {
            return tpCache.get(key);
        }

        @Override
        protected StatCollector getByCode(String code) {
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
                        final Long id = rs.getLong(1);              // ID
                        final String code = rs.getString(2);        // CODE

                        StatCollector sc = new StatCollector(
                                id,
                                code,
                                rs.getString(3),                    // GROUP_BY
                                rs.getString(4),                    // FILTER
                                rs.getDate(5),                      // LAST_ACTIVITY_DT_TM
                                Activity.valueOf(rs.getString(6))   // CODE
                        );

                        addByKey(id, sc);
                        addByCode(code, sc);
                    }
                    log.info("Stat Collectors cached, ttl=" + cacheSize());
                }
            }
        }
    }
}
