package sys.stat;

import com.google.gson.annotations.SerializedName;
import sys.type.TimeInterval;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TimeStatistics implements Serializable {
    private static final long serialVersionUID = 2300433578699252041L;

    @SerializedName("mon_code")
    private String monCode;

    @SerializedName("avg_app_val")
    private BigDecimal avgAppVal;

    @SerializedName("avg_rej_val")
    private BigDecimal avgRejVal;

    @SerializedName("ttl_app_cnt")
    private BigDecimal ttlAppCnt;

    @SerializedName("ttl_app_val")
    private BigDecimal ttlAppVal;

    @SerializedName("ttl_rej_cnt")
    private BigDecimal ttlRejCnt;

    @SerializedName("ttl_rej_val")
    private BigDecimal ttlRejVal;

    @SerializedName("fr_app_cnt")
    private BigDecimal freqAppCnt;

    @SerializedName("fr_app_val")
    private BigDecimal freqAppVal;

    @SerializedName("fr_rej_cnt")
    private BigDecimal freqRejCnt;

    @SerializedName("fr_rej_val")
    private BigDecimal freqRejVal;

    @SerializedName("ls_app_dt")
    private Date lsAppDt;

    @SerializedName("ls_rej_dt")
    private Date lsRejDt;

    private final transient BigDecimal intervalSec;
    private static final transient BigDecimal ONE_CNT = new BigDecimal(1);

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock rLock = rwLock.readLock();
    private final Lock wLock = rwLock.writeLock();

    public TimeStatistics(final String monCode, final TimeInterval interval) {
        this.monCode = monCode;

        this.ttlAppCnt = new BigDecimal(0);
        this.ttlRejCnt = new BigDecimal(0);

        this.ttlAppVal = new BigDecimal(0);
        this.ttlRejVal = new BigDecimal(0);

        this.avgAppVal = new BigDecimal(0);
        this.avgRejVal = new BigDecimal(0);

        this.lsAppDt = null;
        this.lsRejDt = null;

        this.intervalSec = new BigDecimal(interval.getIntervalMs() / 1000);
    }

    public IStatistic get() {
        rLock.lock();
        try {
            return IStatistic.of(avgAppVal, avgRejVal, ttlAppCnt, ttlAppVal, ttlRejCnt, ttlRejVal, freqAppCnt, freqAppVal, freqRejCnt, freqRejVal, lsAppDt, lsRejDt);
        } finally {
            rLock.unlock();
        }
    }

    public void recalculateApproved(final BigDecimal amount, final boolean increment, final Date transDate) {
        wLock.lock();
        try {
            ttlAppCnt = increment ? (ttlAppCnt = ttlAppCnt.add(ONE_CNT)) : (ttlAppCnt = ttlAppCnt.subtract(ONE_CNT));
            ttlAppVal = increment ? (ttlAppVal = ttlAppVal.add(amount)) : (ttlAppVal = ttlAppVal.subtract(amount));
            avgAppVal = ttlAppCnt.intValue() == 0 ? new BigDecimal(0) : (ttlAppVal = ttlAppVal.divide(ttlAppCnt, RoundingMode.UP));
            freqAppCnt = ttlAppCnt.divide(intervalSec, RoundingMode.HALF_UP);
            freqAppVal = ttlAppVal.divide(intervalSec, RoundingMode.UP);
            lsAppDt = getMaxDateValue(lsAppDt, transDate);
        } finally {
            wLock.unlock();
        }
    }

    public void recalculateRejected(final BigDecimal amount, final boolean increment, final Date transDate) {
        wLock.lock();
        try {
            ttlRejCnt = increment ? (ttlRejCnt = ttlRejCnt.add(ONE_CNT)) : (ttlRejCnt = ttlRejCnt.subtract(ONE_CNT));
            ttlRejVal = increment ? (ttlRejVal = ttlRejVal.add(amount)) : (ttlRejVal = ttlRejVal.subtract(amount));
            avgRejVal = ttlRejCnt.intValue() == 0 ? new BigDecimal(0) : ttlRejVal.divide(ttlRejCnt, RoundingMode.UP);
            freqRejCnt = ttlRejCnt.divide(intervalSec, RoundingMode.HALF_UP);
            freqRejVal = ttlRejVal.divide(intervalSec, RoundingMode.UP);
            lsRejDt = getMaxDateValue(lsRejDt, transDate);
        } finally {
            wLock.unlock();
        }
    }

    private Date getMaxDateValue(final Date currentDate, final Date newDate) {
        final Date result;
        if (currentDate == null) {
            result = newDate;
        } else if (newDate == null) {
            result = currentDate;
        } else {
            result = newDate.getTime() > currentDate.getTime() ? newDate : currentDate;
        }
        return result == null ? new Date() : result;
    }

    public String getMonCode() {
        return monCode;
    }

    @Override
    public String toString() {
        return get().toString();
    }

    public boolean isZeroStat() {
        return ttlAppCnt.intValue() == 0 && ttlRejCnt.intValue() == 0;
    }
}
