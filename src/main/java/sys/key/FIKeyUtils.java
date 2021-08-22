package sys.key;

import attribute.BaseAttributes;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.type.StatTransactionData;
import utils.StringUtils;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FIKeyUtils {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static FIKeyCmp buildFIKey(final Set<String> groupBy, final StatTransactionData data) throws Exception {
        final List<Comparable<?>> keysList = new ArrayList<>();
        for (final String attrName : groupBy) {
            int idx = BaseAttributes.getAttributeIdx(attrName);
            if (idx == -1)
                throw new Exception("Can't fetch attribute: " + attrName);

            final String val = data.getValue(idx);
            if (StringUtils.isNullOrEmpty(val)) {
                log.warn("Value of attribute: " + attrName + " is empty");
                return null;
            }

            keysList.add(val.trim());
        }
        return new FIKeyCmp(keysList.toArray(new Comparable[0]));
    }
}
