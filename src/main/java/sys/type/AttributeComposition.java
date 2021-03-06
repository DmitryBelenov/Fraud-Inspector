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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class AttributeComposition implements CacheUnit, IDBType {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final DCAttributeComposition DC_ATTRIBUTE_COMPOSITION = new DCAttributeComposition();

    private static final String TABLE_NAME = "attribute_compositions";
    private static final String[] FIELDS = new String[]{"ID", "CODE", "ATTRIBUTES", "LAST_ACTIVITY_DT_TM", "ACTIVITY"};
    private static final String[] SQL_FIELDS = new String[FIELDS.length - 3];
    static {
        System.arraycopy(FIELDS, 1, SQL_FIELDS, 0, 2);
    }

    private final Map<String, Integer> attrIdxMap;

    private final Long id;

    private final String code;

    private final String[] attributes;

    private final String attributesStr;

    private final Date lastActivityDTm;

    private final Activity activity;

    public AttributeComposition(Long id, String code, String attributesStr, String[] attributes, Date lastActivityDTm, Activity activity) {
        this.id = id;
        this.code = code;
        this.attributesStr = attributesStr;
        this.attributes = attributes;
        this.lastActivityDTm = lastActivityDTm;
        this.activity = activity;

        attrIdxMap = new HashMap<>();
        for (int i = 0; i < attributes.length; i++) {
            String attr = attributes[i];
            if (!StringUtils.isNullOrEmpty(attr)) {
                attrIdxMap.put(attr, i);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getAttributesStr() {
        return attributesStr;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public Date getLastActivityDTm() {
        return lastActivityDTm;
    }

    public Activity getActivity() {
        return activity;
    }

    public int getAttributeIdx(String attribute) {
        return attrIdxMap.getOrDefault(attribute, -1);
    }

    public static DCAttributeComposition getDC() {
        return DC_ATTRIBUTE_COMPOSITION;
    }

    public static AttributeComposition get(Long id) {
        return DC_ATTRIBUTE_COMPOSITION.get(id);
    }

    public static AttributeComposition getByCode(String code) {
        return DC_ATTRIBUTE_COMPOSITION.getByCode(code);
    }

    public static AttributeComposition add(Long id, AttributeComposition tm) {
        return DC_ATTRIBUTE_COMPOSITION.addByKey(id, tm);
    }

    public static Stream<AttributeComposition> getStream() {
        return DC_ATTRIBUTE_COMPOSITION.cacheVStream();
    }

    public static Iterator<AttributeComposition> getIterator() {
        return DC_ATTRIBUTE_COMPOSITION.cacheVIterator();
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
                attributesStr
        );
    }

    private static class DCAttributeComposition extends DBCache<AttributeComposition> {

        public DCAttributeComposition() {
            super();
        }

        @Override
        protected Iterator<AttributeComposition> cacheVIterator() {
            return tpCache.values().iterator();
        }

        @Override
        protected Stream<AttributeComposition> cacheVStream() {
            return tpCache.values().stream();
        }

        @Override
        protected AttributeComposition addByKey(Long key, AttributeComposition type) {
            return tpCache.put(key, type);
        }

        @Override
        protected AttributeComposition addByCode(String code, AttributeComposition type) {
            return codeCache.put(code, type);
        }

        @Override
        protected AttributeComposition get(Long key) {
            return tpCache.get(key);
        }

        @Override
        protected AttributeComposition getByCode(String code) {
            return codeCache.get(code);
        }

        @Override
        protected int cacheSize() {
            return tpCache.size();
        }

        @Override
        public void load() throws SQLException {
            final String sqlAll = "SELECT %s,%s,%s,%s,%s FROM " + TABLE_NAME +" WHERE ACTIVITY = 'A'";
            try (Connection conn = getDbSource().getConnection();
                 PreparedStatement ps = conn.prepareStatement(String.format(sqlAll, (Object[]) FIELDS))) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final Long id = rs.getLong(1);               // ID
                        final String code = rs.getString(2);         // CODE
                        final String attributeStr = rs.getString(3);
                        final String[] attributes = attributeStr.split(","); // ATTRIBUTES

                        AttributeComposition ac = new AttributeComposition(
                                id,
                                code,
                                attributeStr,
                                attributes,
                                rs.getDate(4),                      // LAST_ACTIVITY_DT_TM
                                Activity.valueOf(rs.getString(5))   // ACTIVITY
                        );

                        addByKey(id, ac);
                        addByCode(code, ac);
                    }
                    log.info("Attribute Collections cached, ttl=" + cacheSize());
                }
            }
        }
    }
}
