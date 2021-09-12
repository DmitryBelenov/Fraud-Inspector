package utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.FIParams;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SysReports {
    protected static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final ScheduledExecutorService cpuAct = Executors.newSingleThreadScheduledExecutor();
    private static final ScheduledExecutorService cpuTtl = Executors.newSingleThreadScheduledExecutor();

    public static void startCpuUsageReport() {
        cpuAct.scheduleWithFixedDelay(() -> {
            Optional<String> cpuActRpt = SysUtils.getCpuUsageInfo(true);
            cpuActRpt.ifPresent(log::info);
        }, 100, FIParams.CPU_USAGE_ACTIVITY_REPORT_FREQ_MIN, TimeUnit.MINUTES);

        cpuTtl.scheduleWithFixedDelay(() -> {
            Optional<String> cpuActRpt = SysUtils.getCpuUsageInfo(false);
            cpuActRpt.ifPresent(log::info);
        }, 100, FIParams.CPU_USAGE_TOTAL_REPORT_FREQ_MIN, TimeUnit.MINUTES);

        log.info("CPU usage reporting started");
    }
}
