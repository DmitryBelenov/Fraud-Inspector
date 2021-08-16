package sys.stat;

import java.math.BigDecimal;
import java.util.Date;

public interface IStatistic {

    BigDecimal getAverageApprovedValue();

    BigDecimal getAverageRejectedValue();

    BigDecimal getTotalApprovedCount();

    BigDecimal getTotalApprovedValue();

    BigDecimal getTotalRejectedCount();

    BigDecimal getTotalRejectedValue();

    BigDecimal getFreqApprovedCount();

    BigDecimal getFreqApprovedValue();

    BigDecimal getFreqRejectedCount();

    BigDecimal getFreqRejectedValue();

    Date getLastApprovedDate();

    Date getLastRejectedDate();

        static IStatistic of(final BigDecimal averageApprovedValue,
                             final BigDecimal averageRejectedValue,
                             final BigDecimal totalApprovedCount,
                             final BigDecimal totalApprovedValue,
                             final BigDecimal totalRejectedCount,
                             final BigDecimal totalRejectedValue,
                             final BigDecimal freqApprovedCount,
                             final BigDecimal freqApprovedValue,
                             final BigDecimal freqRejectedCount,
                             final BigDecimal freqRejectedValue,
                             final Date lastApprovedDate,
                             final Date lastRejectedDate) {
            return new IStatistic() {
                @Override
                public BigDecimal getAverageApprovedValue() {
                    return averageApprovedValue;
                }

                @Override
                public BigDecimal getAverageRejectedValue() {
                    return averageRejectedValue;
                }

                @Override
                public BigDecimal getTotalApprovedCount() {
                    return totalApprovedCount;
                }

                @Override
                public BigDecimal getTotalApprovedValue() {
                    return totalApprovedValue;
                }

                @Override
                public BigDecimal getTotalRejectedCount() {
                    return totalRejectedCount;
                }

                @Override
                public BigDecimal getTotalRejectedValue() {
                    return totalRejectedValue;
                }

                @Override
                public BigDecimal getFreqApprovedCount() {
                    return freqApprovedCount;
                }

                @Override
                public BigDecimal getFreqApprovedValue() {
                    return freqApprovedValue;
                }

                @Override
                public BigDecimal getFreqRejectedCount() {
                    return freqRejectedCount;
                }

                @Override
                public BigDecimal getFreqRejectedValue() {
                    return freqRejectedValue;
                }

                @Override
                public Date getLastApprovedDate() {
                    return lastApprovedDate;
                }

                @Override
                public Date getLastRejectedDate() {
                    return lastRejectedDate;
                }

                @Override
                public String toString() {
                    return "[" +
                            "ttlAppCnt="   + getTotalApprovedCount().intValue()   +
                            ",ttlAppVal="  + getTotalApprovedValue().intValue()   +
                            ",ttlRejCnt="  + getTotalRejectedCount().intValue()  +
                            ",ttlRejVal="  + getTotalRejectedValue().intValue()   +
                            ",avgAppVal="  + getAverageApprovedValue().doubleValue() +
                            ",avgRejVal="  + getAverageRejectedValue().doubleValue() +
                            ",freqAppCnt=" + getFreqApprovedCount().doubleValue()    +
                            ",freqAppVal=" + getFreqApprovedValue().doubleValue()    +
                            ",freqRejCnt=" + getFreqRejectedCount().doubleValue()    +
                            ",freqRejVal=" + getFreqRejectedValue().doubleValue()    +
                            ",lastAppDt="  + getLastApprovedDate()     +
                            ",lastRejDt="  + getLastRejectedDate()     +
                            "]";
                }
            };
        }
}
