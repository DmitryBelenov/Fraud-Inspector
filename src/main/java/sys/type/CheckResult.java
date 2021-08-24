package sys.type;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.cache.CacheUnit;
import sys.cache.DBCache;
import sys.type.DeterminingFunction.PostAction;
import utils.StringUtils;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.stream.Stream;

public class CheckResult implements CacheUnit, IDBType {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final DCCheckResult DC_CHECK_RESULT = new DCCheckResult();

    private static final String TABLE_NAME = "check_results";
    private static final String[] FIELDS = new String[]{"ID", "AMOUNT", "IS_SUSPICIOUS", "INFO", "AC_CODE", "ATTRIBUTES", "WEIGHT_ID", "LAST_ACTIVITY_DT_TM", "ACTIVITY"};
    private static final String[] SQL_FIELDS = new String[FIELDS.length - 3];
    static {
        System.arraycopy(FIELDS, 1, SQL_FIELDS, 0, 6);
    }

    private Long id;

    private String amount;

    private boolean isSuspicious;

    private PostAction postAction;

    private String info;

    private String acCode;

    private String attributes;

    private FunctionWeight weight;

    private Date lastActivityDTm;

    private Activity activity;

    public CheckResult(Long id, String amount, boolean isSuspicious, String info, String acCode, String attributes, FunctionWeight weight, Date lastActivityDTm, Activity activity) {
        this.id = id;
        this.amount = amount;
        this.isSuspicious = isSuspicious;
        this.info = info;
        this.acCode = acCode;
        this.attributes = attributes;
        this.weight = weight;
        this.lastActivityDTm = lastActivityDTm;
        this.activity = activity;

        postAction = null;
    }

    public static CheckResult buildFtCheckResult(final Long paymentId, final StatTransactionData data) {
        return null;
    }

    public Long getId() {
        return id;
    }

    public String getAmount() {
        return amount;
    }

    public boolean isSuspicious() {
        return isSuspicious;
    }

    public PostAction getPostAction() {
        return postAction;
    }

    public void setPostAction(PostAction postAction) {
        this.postAction = postAction;
    }

    public String getInfo() {
        return info;
    }

    public String getAcCode() {
        return acCode;
    }

    public String getAttributes() {
        return attributes;
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

    public static DCCheckResult getDC() {
        return DC_CHECK_RESULT;
    }

    public static CheckResult get(Long id) {
        return DC_CHECK_RESULT.get(id);
    }

    public static CheckResult getByCode(String code) {
        return DC_CHECK_RESULT.getByCode(code);
    }

    public static CheckResult add(Long id, CheckResult tm) {
        return DC_CHECK_RESULT.addByKey(id, tm);
    }

    public static Stream<CheckResult> getStream() {
        return DC_CHECK_RESULT.cacheVStream();
    }

    public static Iterator<CheckResult> getIterator() {
        return DC_CHECK_RESULT.cacheVIterator();
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
                amount,
                StringUtils.booleanYNWrapper(isSuspicious),
                info,
                acCode,
                attributes,
                weight.getId()
        );
    }

    private static class DCCheckResult extends DBCache<CheckResult> {

        @Override
        protected Iterator<CheckResult> cacheVIterator() {
            return tpCache.values().iterator();
        }

        @Override
        protected Stream<CheckResult> cacheVStream() {
            return tpCache.values().stream();
        }

        @Override
        protected CheckResult addByKey(Long key, CheckResult type) {
            return tpCache.put(key, type);
        }

        @Override
        protected CheckResult addByCode(String code, CheckResult type) {
            return codeCache.put(code, type);
        }

        @Override
        protected CheckResult get(Long key) {
            return tpCache.get(key);
        }

        @Override
        protected CheckResult getByCode(String code) {
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
                        final Long id = rs.getLong(1);                          // ID
                        FunctionWeight fw = FunctionWeight.get(rs.getLong(7));  // WEIGHT
                        CheckResult sc = new CheckResult(
                                id,
                                rs.getString(2),                                // AMOUNT
                                StringUtils.stringYNWrapper(rs.getString(3)),   // IS_SUSPICIOUS
                                rs.getString(4),                                // INFO
                                rs.getString(5),                                // AC_CODE
                                rs.getString(6),                                // ATTRIBUTES
                                fw,
                                rs.getDate(8),                                  // LAST_ACTIVITY_DT_TM
                                Activity.valueOf(rs.getString(9))               // ACTIVITY
                        );
                        addByKey(id, sc);
                    }
                    log.info("Check results cached, ttl=" + cacheSize());
                }
            }
        }
    }
}
