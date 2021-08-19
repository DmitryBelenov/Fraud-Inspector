package kafka;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utils.SysUtils;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class PaymentLoader implements Runnable {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final AtomicBoolean LOADING = new AtomicBoolean(true);

    public static void start() {
        final ExecutorService executor = SysUtils.newFixedThreadPool("Pmt-NoDelay-Loader", 1);
        executor.execute(new PaymentLoader());
    }



    @Override
    public void run() {

    }
}
