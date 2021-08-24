package sys;

import kafka.BootstrapStatLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.type.CheckResult;
import sys.type.StatTransactionData;
import sys.type.TimeMonitor;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
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
            final CheckResult result = CheckResult.buildFtCheckResult(paymentId, data);
            try {
//                if (!result.isError()) {
                    // calculate result
                    calculateResult(data, result);
                    log.info("Apply transaction with id:" + paymentId + " to statistics");
                    Iterator<TimeMonitor> tmIt = TimeMonitor.getIterator();
                    while (tmIt.hasNext()) {
                        TimeMonitor tm = tmIt.next();
                        tm.addToStatistics(data);
                    }
//                }
            } catch (final Exception e) {
//                result.markError(e);
            }

            return result;
        });
    }

    private static void calculateResult(final StatTransactionData data, final CheckResult result) throws Exception {
//        long riskLevelSum = 0L;
//        final Date checkDateTime = new Date();
//        for (final FTFraudCheckCondition checkCondition : FraudCheckConditionHolder.getConditions()) {
//            result.setCheckDateTime(checkDateTime);
//            final FTRiskWeight riskWeight = checkCondition.getRiskWeight();
//
//            final boolean increaseRiskLevel = checkCondition.getConditionResult(data, result, riskLevelSum);
//
//            if (result.isError())
//                break;
//
//            if (increaseRiskLevel) {
//                riskLevelSum += riskWeight.getWeight();
//
//                if (DefaultPropertyCore.isEnableDevTrace) {
//                    log.info("Add triggered condition id:" + checkCondition.id + " to check result id:" + result.id);
//                }
//            }
//
//            if (riskLevelReached(riskLevelSum, result)) {
//                result.setRiskWeight(riskWeight);
//                result.setActionType(checkCondition.action);
//                break;
//            }
//        }
//
//        if (triggeredConditions.size() > 0) {
//            result.setTriggeredConditions(triggeredConditions);
//        }
//        result.setTtlRiskWeight(riskLevelSum);
    }

//    private static boolean riskLevelReached(long riskLevelSum, FTCheckResult result) {
//        if (riskLevelSum >= DefaultPropertyFD.riskLevelLimit) {
//            result.markFraud();
//            return true;
//        }
//
//        return riskLevelSum <= DefaultPropertyFD.lowerRiskLevelLimit;
//    }
}
