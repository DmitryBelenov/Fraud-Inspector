package sys.type;

import com.google.gson.annotations.SerializedName;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import service.HttpProcessor;

import javax.servlet.AsyncContext;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;

public class TransportTransactionData implements Serializable, IRestore {
    private static final long serialVersionUID = -4776601442269796108L;
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @SerializedName("stat_tr_data")
    public StatTransactionData data;

    @SerializedName("tr_id")
    public Long id;
    transient AsyncContext respCtx;

    public TransportTransactionData(StatTransactionData data, Long id) {
        this.data = data;
        this.id = id;
    }

    public StatTransactionData getData() {
        return data;
    }

    public Long getId() {
        return id;
    }

    public AsyncContext getRespCtx() {
        return respCtx;
    }

    @Override
    public void restore() {
        respCtx = HttpProcessor.acCtxMap.remove(id);
        if (respCtx != null) {
            log.error("Async context, id:" + id + ", restored");
        }
    }
}
