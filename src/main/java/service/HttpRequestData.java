package service;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class HttpRequestData implements Serializable {
    private static final long serialVersionUID = 1780337427884452843L;

    @SerializedName("t")
    private HttpRequestType type;

    @SerializedName("att")
    private String attrCode;

    @SerializedName("pm")
    private Map<String, String> pmData;

    public HttpRequestType getType() {
        return type;
    }

    public Map<String, String> getPmData() {
        return pmData;
    }

    public String getAttrCode() {
        return attrCode;
    }
}
