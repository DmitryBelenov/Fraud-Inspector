package sys.stat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

public interface IStatistic {

    BigDecimal getAverageApprovedValue();

    BigDecimal getAverageRejectedValue();

    long getTotalApprovedCount();

    BigDecimal getTotalApprovedValue();

    long getTotalRejectedCount();

    BigDecimal getTotalRejectedValue();

    long getFreqApprovedCount();

    BigDecimal getFreqApprovedValue();

    long getFreqRejectedCount();

    BigDecimal getFreqRejectedValue();

    Date getLastApprovedDate();

    Date getLastRejectedDate();

        static IStatistic of(final BigDecimal averageApprovedValue,
                             final BigDecimal averageRejectedValue,
                             final long totalApprovedCount,
                             final BigDecimal totalApprovedValue,
                             final long totalRejectedCount,
                             final BigDecimal totalRejectedValue,
                             final long freqApprovedCount,
                             final BigDecimal freqApprovedValue,
                             final long freqRejectedCount,
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
                public long getTotalApprovedCount() {
                    return totalApprovedCount;
                }

                @Override
                public BigDecimal getTotalApprovedValue() {
                    return totalApprovedValue;
                }

                @Override
                public long getTotalRejectedCount() {
                    return totalRejectedCount;
                }

                @Override
                public BigDecimal getTotalRejectedValue() {
                    return totalRejectedValue;
                }

                @Override
                public long getFreqApprovedCount() {
                    return freqApprovedCount;
                }

                @Override
                public BigDecimal getFreqApprovedValue() {
                    return freqApprovedValue;
                }

                @Override
                public long getFreqRejectedCount() {
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
                            "ttlAppCnt="   + getTotalApprovedCount()   +
                            ",ttlAppVal="  + getTotalApprovedValue().intValue()   +
                            ",ttlRejCnt="  + getTotalRejectedCount()  +
                            ",ttlRejVal="  + getTotalRejectedValue().intValue()   +
                            ",avgAppVal="  + getAverageApprovedValue().doubleValue() +
                            ",avgRejVal="  + getAverageRejectedValue().doubleValue() +
                            ",freqAppCnt=" + getFreqApprovedCount()    +
                            ",freqAppVal=" + Optional.ofNullable(getFreqApprovedValue()).map(BigDecimal::doubleValue).orElse(0d)    +
                            ",freqRejCnt=" + getFreqRejectedCount()    +
                            ",freqRejVal=" + Optional.ofNullable(getFreqRejectedValue()).map(BigDecimal::doubleValue).orElse(0d)    +
                            ",lastAppDt="  + getLastApprovedDate()     +
                            ",lastRejDt="  + getLastRejectedDate()     +
                            "]";
                }
            };
        }
}
