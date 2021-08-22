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
import java.util.Date;
import java.util.Iterator;
import java.util.stream.Stream;

public class FunctionWeight implements CacheUnit, IDBType {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final DCFunctionWeight DC_FUNCTION_WEIGHT = new DCFunctionWeight();

    private static final String TABLE_NAME = "function_weights";
    private static final String[] FIELDS = new String[]{"ID", "CODE", "WEIGHT", "PRIORITY", "LAST_ACTIVITY_DT_TM", "ACTIVITY"};
    private static final String[] SQL_FIELDS = new String[FIELDS.length - 3];
    static {
        System.arraycopy(FIELDS, 1, SQL_FIELDS, 0, 3);
    }

    private final Long id;

    private final String code;

    private final Long weight;

    private final Long priority;

    private final Date lastActivityDTm;

    private final Activity activity;

    public FunctionWeight(Long id, String code, Long weight, Long priority, Date lastActivityDTm, Activity activity) {
        this.id = id;
        this.code = code;
        this.weight = weight;
        this.priority = priority;
        this.lastActivityDTm = lastActivityDTm;
        this.activity = activity;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Long getWeight() {
        return weight;
    }

    public Long getPriority() {
        return priority;
    }

    public Date getLastActivityDTm() {
        return lastActivityDTm;
    }

    public Activity getActivity() {
        return activity;
    }

    public static DCFunctionWeight getDC() {
        return DC_FUNCTION_WEIGHT;
    }

    public static FunctionWeight get(Long id) {
        return DC_FUNCTION_WEIGHT.get(id);
    }

    public static FunctionWeight getByCode(String code) {
        return DC_FUNCTION_WEIGHT.getByCode(code);
    }

    public static FunctionWeight add(Long id, FunctionWeight tm) {
        return DC_FUNCTION_WEIGHT.addByKey(id, tm);
    }

    public static Stream<FunctionWeight> getStream() {
        return DC_FUNCTION_WEIGHT.cacheVStream();
    }

    public static Iterator<FunctionWeight> getIterator() {
        return DC_FUNCTION_WEIGHT.cacheVIterator();
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
                weight,
                priority
        );
    }

    private static class DCFunctionWeight extends DBCache<FunctionWeight> {

        public DCFunctionWeight() {
            super();
        }

        @Override
        protected Iterator<FunctionWeight> cacheVIterator() {
            return tpCache.values().iterator();
        }

        @Override
        protected Stream<FunctionWeight> cacheVStream() {
            return tpCache.values().stream();
        }

        @Override
        protected FunctionWeight addByKey(Long key, FunctionWeight type) {
            return tpCache.put(key, type);
        }

        @Override
        protected FunctionWeight addByCode(String code, FunctionWeight type) {
            return codeCache.put(code, type);
        }

        @Override
        protected FunctionWeight get(Long key) {
            return tpCache.get(key);
        }

        @Override
        protected FunctionWeight getByCode(String code) {
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
                        final Long id = rs.getLong(1);                  // ID
                        final String code = rs.getString(2);            // CODE

                        FunctionWeight sc = new FunctionWeight(
                                id,
                                code,
                                rs.getLong(3),                          // WEIGHT
                                rs.getLong(4),                          // PRIORITY
                                rs.getDate(5),                          // LAST_ACTIVITY_DT_TM
                                Activity.valueOf(rs.getString(9))       // ACTIVITY
                        );

                        addByKey(id, sc);
                        addByCode(code, sc);
                    }
                    log.info("Function weights cached, ttl=" + cacheSize());
                }
            }
        }
    }
}
