package sys;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.type.*;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.*;

public class ResultExtractor {
    protected static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final ExecutorService RES_SRV = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static void shutdownES() {
        if (!RES_SRV.isShutdown())
            RES_SRV.shutdown();
    }

    public static CheckResult extract(final Long paymentId, final StatTransactionData data) throws Exception {
        final Future<CheckResult> resultFuture = getAsyncResult(paymentId, data);
        final CheckResult result;
        try {
            result = resultFuture.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new Exception("FI System internal error", e);
        }
        return result;
    }

    private static Future<CheckResult> getAsyncResult(final Long paymentId, final StatTransactionData data) {
        return RES_SRV.submit(() -> {
            final CheckResult result = CheckResult.buildFtCheckResult(data);
            try {
                calculateResult(data, result);
                data.setSuspicious(result.isSuspicious());
                log.info("Applying transaction with id:" + paymentId + " to statistics");
                Iterator<TimeMonitor> tmIt = TimeMonitor.getIterator();
                while (tmIt.hasNext()) {
                    TimeMonitor tm = tmIt.next();
                    tm.addToStatistics(data);
                }
            } catch (final Exception e) {
                result.markError(e);
            }

            return result;
        });
    }

    private static void calculateResult(final StatTransactionData data, final CheckResult result) throws Exception {
        Long ttlWeight = 0L;
        Iterator<DeterminingFunction> dfIt = DeterminingFunction.getIterator();
        while (dfIt.hasNext()) {
            final DeterminingFunction dFunc = dfIt.next();
            final FunctionWeight fWeight = dFunc.getWeight();

            final boolean increaseTtlWeight = dFunc.getResult(data, result, ttlWeight);

            if (result.isError())
                break;

            if (increaseTtlWeight) {
                ttlWeight += fWeight.getWeight();
            }

            if (riskLevelReached(ttlWeight, result)) {
                result.setPostAction(Optional.ofNullable(dFunc.getPostAction()).orElse(DeterminingFunction.PostAction.APPROVE));
                result.setWeight(fWeight);
                break;
            }
        }
    }

    private static boolean riskLevelReached(long ttlWeight, CheckResult result) {
        if (ttlWeight >= FIParams.TTL_WEIGHT_LIMIT) {
            result.markSuspicious();
            return true;
        }

        return ttlWeight <= FIParams.LOWER_TTL_WEIGHT_LIMIT;
    }
}
