package sys.type;

import com.google.gson.annotations.SerializedName;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.cache.AttributeHolder;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

public class StatTransactionData implements Serializable, IRestore {
    private static final long serialVersionUID = -7700672789719088291L;
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @SerializedName("attr_code")
    public String attrCode;
    private transient AttributeComposition ac;

    @SerializedName("attr_values")
    public String[] attrValues;

    @SerializedName("suspicious")
    public Boolean isSuspicious;

    public StatTransactionData() {
    }

    public StatTransactionData(String attrCode, String[] attrValues) {
        this.attrCode = attrCode;
        this.attrValues = attrValues;
        this.ac = AttributeHolder.getByCode(attrCode);
    }

    public String getValue(int idx) throws Exception {
        if (idx > attrValues.length - 1)
            throw new Exception("Wrong index " + idx + " of attrCmp: " + attrCode);

        return attrValues[idx];
    }

    @Override
    public void restore() {
        if (ac == null) {
            ac = AttributeHolder.getByCode(attrCode);
        }
    }

    public String[] getAttrValues() {
        return attrValues;
    }

    public Boolean getSuspicious() {
        return isSuspicious;
    }

    public void setSuspicious(Boolean suspicious) {
        isSuspicious = suspicious;
    }

    public AttributeComposition getAc() {
        return ac;
    }
}
