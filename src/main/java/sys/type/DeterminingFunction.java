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

public class DeterminingFunction implements CacheUnit, IDBType {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final DCDeterminingFunction DC_DETERMINING_FUNCTION = new DCDeterminingFunction();

    private static final String TABLE_NAME = "determining_functions";
    private static final String[] FIELDS = new String[]{"ID", "CODE", "POST_ACTION", "PRE_FUNCTION", "FUNCTION", "INFO", "WEIGHT_ID", "LAST_ACTIVITY_DT_TM", "ACTIVITY"};
    private static final String[] SQL_FIELDS = new String[FIELDS.length - 3];
    static {
        System.arraycopy(FIELDS, 1, SQL_FIELDS, 0, 6);
    }

    private final Long id;

    private final String code;

    private final PostAction postAction;

    private final String preFunction;

    private final String function;

    private final String info;

    private final FunctionWeight weight;

    private final Date lastActivityDTm;

    private final Activity activity;

    public enum PostAction {
        APPROVE("A"), REJECT("R"), WARNING("W"), AUTH("AU");
        private final String shortName;

        PostAction(String shortName) {
            this.shortName = shortName;
        }

        public String getShortName() {
            return shortName;
        }
    }

    public DeterminingFunction(Long id, String code, PostAction postAction, String preFunction, String function, String info, FunctionWeight weight, Date lastActivityDTm, Activity activity) {
        this.id = id;
        this.code = code;
        this.postAction = postAction;
        this.preFunction = preFunction;
        this.function = function;
        this.info = info;
        this.weight = weight;
        this.lastActivityDTm = lastActivityDTm;
        this.activity = activity;
    }

    public boolean getResult(StatTransactionData stData, CheckResult result, long ttlWeight) throws Exception {
        result.setInfo(getInfo());
        return true;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public PostAction getPostAction() {
        return postAction;
    }

    public String getPreFunction() {
        return preFunction;
    }

    public String getFunction() {
        return function;
    }

    public String getInfo() {
        return info;
    }

    public FunctionWeight getWeight() {
        return weight;
    }

    public Date getLastActivityDTm() {
        return lastActivityDTm;
    }

    public Activity getActivity() {
        return activity;
    }

    public static DCDeterminingFunction getDC() {
        return DC_DETERMINING_FUNCTION;
    }

    public static DeterminingFunction get(Long id) {
        return DC_DETERMINING_FUNCTION.get(id);
    }

    public static DeterminingFunction getByCode(String code) {
        return DC_DETERMINING_FUNCTION.getByCode(code);
    }

    public static DeterminingFunction add(Long id, DeterminingFunction tm) {
        return DC_DETERMINING_FUNCTION.addByKey(id, tm);
    }

    public static Stream<DeterminingFunction> getStream() {
        return DC_DETERMINING_FUNCTION.cacheVStream();
    }

    public static Iterator<DeterminingFunction> getIterator() {
        return DC_DETERMINING_FUNCTION.cacheVIterator();
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
                postAction.name(),
                preFunction,
                function,
                info,
                weight.getId()
        );
    }

    private static class DCDeterminingFunction extends DBCache<DeterminingFunction> {

        public DCDeterminingFunction() {
            super();
        }

        @Override
        protected Iterator<DeterminingFunction> cacheVIterator() {
            return tpCache.values().iterator();
        }

        @Override
        protected Stream<DeterminingFunction> cacheVStream() {
            return tpCache.values().stream();
        }

        @Override
        protected DeterminingFunction addByKey(Long key, DeterminingFunction type) {
            return tpCache.put(key, type);
        }

        @Override
        protected DeterminingFunction addByCode(String code, DeterminingFunction type) {
            return codeCache.put(code, type);
        }

        @Override
        protected DeterminingFunction get(Long key) {
            return tpCache.get(key);
        }

        @Override
        protected DeterminingFunction getByCode(String code) {
            return codeCache.get(code);
        }

        @Override
        protected int cacheSize() {
            return tpCache.size();
        }

        @Override
        public void load() throws SQLException {
            final String sqlAll = "SELECT %s,%s,%s,%s,%s,%s,%s,%s,%s FROM " + TABLE_NAME +" WHERE ACTIVITY = 'A'";
            try (Connection conn = getDbSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement(String.format(sqlAll, (Object[]) FIELDS))) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final Long id = rs.getLong(1);                  // ID
                        final String code = rs.getString(2);            // CODE

                        FunctionWeight fw = FunctionWeight.get(rs.getLong(7)); // WEIGHT
                        DeterminingFunction sc = new DeterminingFunction(
                                id,
                                code,
                                PostAction.valueOf(rs.getString(3)),    // POST_ACTION
                                rs.getString(4),                        // PRE_FUNCTION
                                rs.getString(5),                        // FUNCTION
                                rs.getString(6),                        // INFO
                                fw,
                                rs.getDate(8),                          // LAST_ACTIVITY_DT_TM
                                Activity.valueOf(rs.getString(9))       // ACTIVITY
                        );

                        addByKey(id, sc);
                        addByCode(code, sc);
                    }
                    log.info("Determining functions cached, ttl=" + cacheSize());
                }
            }
        }
    }
}
