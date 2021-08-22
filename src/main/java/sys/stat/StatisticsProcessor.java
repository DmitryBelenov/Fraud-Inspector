package sys.stat;

import attribute.BaseAttributes;
import attribute.RequiredField;
import attribute.ServiceField;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.key.FIKeyCmp;
import sys.key.FIKeyUtils;
import sys.type.StatTransactionData;
import sys.type.TimeMonitor;
import utils.StringUtils;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class StatisticsProcessor {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);

    public static void addToStatistics(final TimeMonitor timeMonitor, final StatTransactionData data) {
        calculate(timeMonitor, data, ProcessionType.ADD);
    }

    public static void removeFromStatistics(final TimeMonitor timeMonitor, final StatTransactionData data) {
        calculate(timeMonitor, data, ProcessionType.REMOVE);
    }

    private static void calculate(final TimeMonitor timeMonitor, final StatTransactionData data, final ProcessionType type) {
        final Boolean isSuspicious = data.getSuspicious();
        if (isSuspicious == null) {
            log.error("Statistics calculation error, suspicious flag not found");
            return;
        }

        final boolean increment = type == ProcessionType.ADD;
        final Set<String> groupBySet = timeMonitor.getCollector().getGroupBySet();
        final FIKeyCmp key;
        try {
            key = FIKeyUtils.buildFIKey(groupBySet, data);
            if (key == null) {
                return;
            }
        } catch (Exception e) {
            log.error("Statistics calculation error", e);
            return;
        }

        final BigDecimal amount;
        final Date transactionDate;
        try {
            int amtIdx = BaseAttributes.getAttributeIdx(RequiredField.Amt.name());
            amount = new BigDecimal(data.getValue(amtIdx));

            int dtIdx = BaseAttributes.getAttributeIdx(ServiceField.PmDtTm.name());
            final String date = data.getValue(dtIdx);

            transactionDate = (StringUtils.isNullOrEmpty(date)) ? new Date() : DATE_FORMAT.parse(date);
        } catch (Exception e) {
            log.error("Statistics calculation error", e);
            return;
        }

        timeMonitor.getStatistics().compute(key, (k, stat) -> {
            final String monCode = timeMonitor.getCode();
            if (stat == null) {
                stat = new TimeStatistics(monCode, timeMonitor.getInterval());
            }

            if (isSuspicious) {
                stat.recalculateRejected(amount, increment, transactionDate);
            } else {
                stat.recalculateApproved(amount, increment, transactionDate);
            }

            final TimeStatistics ts = stat;
            log.info((increment ? "increment " : "decrement ")
                    + monCode
                    + " [gb:" + timeMonitor.getCollector().getGroupBy()
                    + ", f:"  + timeMonitor.getCollector().getFilter() +"] val: " + amount + " key: " + key + " new stats: " + ts);

            if (!increment && stat.isZeroStat()) {
                log.trace("Statistics cleaning: " + ts + " was removed by key:[" + key + "] from monitor:" + monCode);
                return null;
            }
            return stat;
        });
    }

    enum ProcessionType {
        ADD,
        REMOVE
    }
}
