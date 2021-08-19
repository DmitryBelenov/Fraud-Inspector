package service;

import attribute.RequiredField;
import kafka.BootstrapStatLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.cache.AttributeHolder;
import sys.type.StatTransactionData;
import sys.type.TransportTransactionData;
import utils.JsonUtils;
import utils.StringUtils;
import utils.SysUtils;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpProcessor {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static final Map<Long, AsyncContext> acCtxMap = new ConcurrentHashMap<>();
    public static final String DATA = "data";

    public static void getResponse(HttpServletRequest req, HttpServletResponse resp, boolean isPost) {
            resp.setContentType("application/json; charset=utf-8");

            final String data = getParameter(req, DATA);
            final HttpRequestData httpReq = JsonUtils.fromJson(data, HttpRequestData.class);

            final AsyncContext ac = req.startAsync();
            if (httpReq.getType() == HttpRequestType.IsSuspected) {
                checkIsPmtSuspected(httpReq.getPmData(), ac, resp, httpReq.getAttrCode());
            }
    }

    private static void checkIsPmtSuspected(Map<String, String> pmData, AsyncContext ac, HttpServletResponse resp, String attrCode) {
        try {
            long id = 0L;
            final String idStr = pmData.getOrDefault(RequiredField.Id.name(), "");
            if (StringUtils.isNotEmpty(idStr)) {
                id = Long.parseLong(idStr);
            }
            if (id == 0)
                throw new Exception("Transaction unique identifier is missed");

            if (BootstrapStatLoader.RECOVERED_KEYS.contains(String.valueOf(id)))
                throw new Exception("Transaction with id:" + id + " was already handled by FI Service");

            acCtxMap.put(id, ac);

            final String[] attributes = AttributeHolder.getIndexedAttrValues(pmData, attrCode);
            TransportTransactionData tData = new TransportTransactionData(new StatTransactionData(attrCode, attributes), id);

            // call check method
        } catch (Exception e) {
            log.error("FI System check error", e);
            writeSyncReply(resp, new HttpReply(false, "N/A", "N/A", "FI System internal error: " + e.getMessage()));
        }
    }

    public static void writeAsyncReply(Long id, AsyncContext ac, HttpReply reply) {
        if (ac != null) {
            final ServletResponse resp = ac.getResponse();
            final String replyText = JsonUtils.toJson(reply);
            try (final ServletOutputStream sOut = resp.getOutputStream()) {
                SysUtils.write2stream(sOut, replyText);
                sOut.println();
                sOut.flush();
                log.info("Response sent to async context, id:" + id + ", txt:" + replyText);
            } catch (IOException ex) {
                log.error("Async response error", ex);
            }
        } else {
            log.error("Async context is null, id:" + id);
        }
    }

    public static void writeSyncReply(HttpServletResponse resp, HttpReply reply) {
        final String replyText = JsonUtils.toJson(reply);
        try (final ServletOutputStream sOut = resp.getOutputStream()) {
            SysUtils.write2stream(sOut, replyText);
            sOut.println();
            sOut.flush();
            log.info("Sync response sent, txt:" + replyText);
        } catch (IOException ex) {
            log.error("Sync response error", ex);
        }
    }

    private static String getParameter(final HttpServletRequest req, final String name) {
        return req != null ? req.getParameter(name) : null;
    }
}
