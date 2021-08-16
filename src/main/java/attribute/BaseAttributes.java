package attribute;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseAttributes {

    public static final Map<String, Integer> ATTRIB_IDX = new ConcurrentHashMap<>();

    private static final String[] ATTRIBUTES = new String[] {
            RequiredField.Id.name(),
            RequiredField.Amt.name(),
            RequiredField.Ccy.name(),
            RequiredField.Dbt_IBAN.name(),
            RequiredField.Crd_IBAN.name()
    };

    static {
        for (int i = 0; i < ATTRIBUTES.length; i++) {
            ATTRIB_IDX.put(ATTRIBUTES[i], i);
        }
    }
}
