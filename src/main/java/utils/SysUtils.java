package utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.FIParams;
import sys.FISystem;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
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
}
