package utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.FIParams;
import sys.FISystem;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SysUtils {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final int defaultBufferSize = 0x4000;

    private static final class SingletonCharset {
        private final Charset charset = Charset.forName(FIParams.httpEncoding);

        private static final class SingletonHolder {
            public static final SingletonCharset INSTANCE = new SingletonCharset();
        }

        public static SingletonCharset getInstance() {
            return SingletonHolder.INSTANCE;
        }
    }

    /**
     * Base method for create threads and put into the pool
     * */
    public static ExecutorService newFixedThreadPool(final String name, final int size) {
        final ExecutorService s = Executors.newFixedThreadPool(size,
                new ThreadFactory() {
                    private final AtomicInteger num = new AtomicInteger();

                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                        thread.setDaemon(true);
                        thread.setName(name + "-" + num.getAndAdd(1) + "/" + size);
                        return thread;
                    }
                });
        FISystem.registerExecutorService(s);
        return s;
    }

    public static void write2stream(final OutputStream outputStream, final String text) throws IOException {
        final CharsetEncoder encoder = getHttpEncodingCharset().newEncoder();

        final int fullLen = text.length();
        final int byteBufferSize = Math.min(defaultBufferSize, fullLen);

        final int en = (int) (byteBufferSize * (double) encoder.maxBytesPerChar());
        final byte[] byteArray = new byte[en];

        final ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        final CharBuffer charBuffer = CharBuffer.wrap(text);
        for (int pos = 0, len = byteBufferSize;
             pos < fullLen;
             pos = len, len = Math.min(pos + byteBufferSize, fullLen)) {

            try {
                final CharBuffer window = charBuffer.subSequence(pos, len);

                CoderResult res = encoder.encode(window, byteBuffer, true);

                if (!res.isUnderflow())
                    res.throwException();

                res = encoder.flush(byteBuffer);

                if (!res.isUnderflow())
                    res.throwException();

            } catch (final CharacterCodingException x) {
                throw new Error(x);
            }

            outputStream.write(byteArray, 0, byteBuffer.position());

            byteBuffer.clear();
            encoder.reset();
        }
    }

    public static Charset getHttpEncodingCharset() {
        return SingletonCharset.getInstance().charset;
    }


    private static final ThreadMXBean TH_MX_BEAN = ManagementFactory.getThreadMXBean();
    private static final Map<Long, Long> thTtlCpuUsages = new ConcurrentHashMap<>();
    private static final DecimalFormat df = new DecimalFormat("#.##");
    /**
     * App CPU usage report method
     *
     * @return optional log string
     **/
    public static Optional<String> getCpuUsageInfo(boolean isShort) {
        Map<Thread, StackTraceElement[]> allThMap = Thread.getAllStackTraces();
        if (allThMap.isEmpty()) {
            return Optional.empty();
        }

        final Map<Long, Thread> activityMap = new TreeMap<>(Collections.reverseOrder());
        final Map<Long, Thread> ttlUsageMap = new TreeMap<>(Collections.reverseOrder());
        allThMap.keySet().forEach(th -> {
            final long thId = th.getId();
            final Long currentTtlCpuTm = TH_MX_BEAN.getThreadCpuTime(thId);
            thTtlCpuUsages.compute(thId, (Id, lastTtlCpuTm) -> {
                if (lastTtlCpuTm != null) {
                    final long activity = currentTtlCpuTm - lastTtlCpuTm;
                    if (activity > 0) {
                        activityMap.put(activity, th);
                    }
                }
                return currentTtlCpuTm;
            });
            if (!isShort) {
                ttlUsageMap.put(currentTtlCpuTm, th);
            }
        });

        if (activityMap.isEmpty()) {
            return Optional.empty();
        }

        StringBuilder actSb = new StringBuilder();
        int topCnt = 0;
        for (Map.Entry<Long, Thread> me : activityMap.entrySet()) {
            if (topCnt >= FIParams.gwTopCPUUsagesCount) {
                break;
            }
            final String thName = me.getValue().getName();
            final long thConsumesNs = me.getKey();
            actSb.append("\t");
            actSb.append("th: ");
            actSb.append(thName);
            actSb.append(", ns:");
            actSb.append(thConsumesNs);
            actSb.append(", ms:");
            actSb.append(df.format((double) thConsumesNs / 1_000_000));
            actSb.append(", sec:");
            actSb.append(df.format((double) thConsumesNs / 1_000_000_000));
            actSb.append("\n");
            topCnt++;
        }
        final String activityReport = "Top " + topCnt + " CPU usage activity:\n" + actSb.toString();

        boolean addTtlReport = !ttlUsageMap.isEmpty();
        String commonReport = null;
        if (addTtlReport) {
            StringBuilder ttlSb = new StringBuilder();
            topCnt = 0;
            for (Map.Entry<Long, Thread> me : ttlUsageMap.entrySet()) {
                if (topCnt >= FIParams.gwTopCPUUsagesCount) {
                    break;
                }
                final String thName = me.getValue().getName();
                final long thConsumesNs = me.getKey();
                ttlSb.append("\t");
                ttlSb.append("th: ");
                ttlSb.append(thName);
                ttlSb.append(", ns:");
                ttlSb.append(thConsumesNs);
                ttlSb.append(", ms:");
                ttlSb.append(df.format((double) thConsumesNs / 1_000_000));
                ttlSb.append(", sec:");
                ttlSb.append(df.format((double) thConsumesNs / 1_000_000_000));
                ttlSb.append("\n");
                topCnt++;
            }
            commonReport = activityReport + "\n\tTop " + topCnt + " total CPU usages:\n" + ttlSb.toString();
        }
        return Optional.of(addTtlReport ? commonReport : activityReport);
    }
}
