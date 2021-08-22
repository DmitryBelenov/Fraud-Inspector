package sys.type;

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
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StatCollector implements CacheUnit, IDBType {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final DCStatCollector DC_STAT_COLLECTOR = new DCStatCollector();

    private static final String TABLE_NAME = "stat_collectors";
    private static final String[] FIELDS = new String[]{"ID", "CODE", "GROUP_BY", "FILTER", "LAST_ACTIVITY_DT_TM", "ACTIVITY"};
    private static final String[] SQL_FIELDS = new String[FIELDS.length - 3];
    static {
        System.arraycopy(FIELDS, 1, SQL_FIELDS, 0, 3);
    }

    private final Lock lock = new ReentrantLock();

    private final Long id;

    private final String code;

    private final String groupBy;
    private Set<String> groupBySet;

    private final String filter;
    private Predicate<StatTransactionData> filterPredicate = t -> true;

    private final Date lastActivityDTm;

    private final Activity activity;

    public StatCollector(Long id, String code, String groupBy, String filter, Date lastActivityDTm, Activity activity) {
        this.id = id;
        this.code = code;
        this.groupBy = groupBy;
        this.filter = filter;
        this.lastActivityDTm = lastActivityDTm;
        this.activity = activity;

        initGroupBy();
        initWhereFilter();
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

    public Set<String> getGroupBySet() {
        return groupBySet;
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

    public boolean filter(final StatTransactionData data) {
        lock.lock();
        try {
            return filterPredicate.test(data);
        } finally {
            lock.unlock();
        }
    }

    private void initGroupBy() {
        if (groupBy == null) {
            this.groupBySet = Collections.emptySet();
        } else {
            this.groupBySet = new TreeSet<>(StringUtils.breakString(groupBy, StringUtils.PARAM_DELIMITERS));
        }
    }

    private void initWhereFilter() {
        if (filter == null) {
            filterPredicate = t -> true;
        } else {
//            final Jep jep = UtilsJepFrd.createParser(statWhere);
//            whereFilter = t -> {
//                try {
//                    try {
//                        final Object conditionResult = UtilsJep.evaluate(jep, new TransactionDataContext(t), true);
//                        if (conditionResult == null) {
//                            throw new SSYSException(SSYSFDEx.CONDITION_RETURNED_NULL, statWhere);
//                        }
//                        return (Boolean) conditionResult;
//                    } catch (final ClassCastException e) {
//                        Utils.printException(log, e);
//                        throw new SSYSException(SSYSFDEx.CONDITION_INVALID_RETURN_TYPE, "", e);
//                    }
//                } catch (final SSYSException e) {
//                    log.error("Can't apply transaction to filter", e);
//                    SSYSGw.stopWork(e);
//                    return false;
//                }
//            };
        }
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
                groupBy,
                filter
        );
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
                                Activity.valueOf(rs.getString(6))   // ACTIVITY
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
