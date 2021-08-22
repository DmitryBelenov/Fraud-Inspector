package service;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class HttpReply implements Serializable {
    private static final long serialVersionUID = 3899679671308844769L;

    @SerializedName("r")
    public boolean resultOK;

    @SerializedName("s")
    public String isSuspicious;

    @SerializedName("a")
    public String action;

    @SerializedName("rs")
    public String reason;

    @SerializedName("dt")
    public Date date;

    public HttpReply(boolean resultOK, String isSuspicious, String action, String reason) {
        this.resultOK = resultOK;
        this.isSuspicious = isSuspicious;
        this.action = action;
        this.reason = reason;
        this.date = new Date();
    }
}
