package code.maxwell.vialinks_dev.server;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

public class HttpExchange {
    private static final String TAG = "HttpExchange";
    private String requestURIPath = null;
    private HashMap<String, String> requestHeaders = new HashMap<>();
    private HashMap<String, String> responseHeaders = new HashMap<>();
    private InputStream requestBody = null;
    private OutputStream responseBody = null;
    private boolean areResponseHeadersSent = false;

    public void sendResponseHeaders(String code, int contentLength) {
        PrintWriter printWriter = new PrintWriter(responseBody);
        printWriter.println(code);
        printWriter.println("Content-Length:" + contentLength);
        Set<String> keys = responseHeaders.keySet();
        for (String key : keys) {
            printWriter.println(key + ":" + responseHeaders.get(key));
        }
        printWriter.println();
        printWriter.flush();
        areResponseHeadersSent = true;
        Log.d(TAG, "sendResponseHeaders: >> Sent Response Headers");
    }

    void setRequestURIPath(String requestURIPath) {
        this.requestURIPath = requestURIPath;
    }

    void setRequestBody(InputStream requestBody) {
        this.requestBody = requestBody;
    }

    void setResponseBody(OutputStream responseBody) {
        this.responseBody = responseBody;
    }

    public String getRequestURIPath() {
        return requestURIPath;
    }

    public HashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public HashMap<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public InputStream getRequestBody() {
        return requestBody;
    }

    public OutputStream getResponseBody() {
        if (!areResponseHeadersSent) {
            throw new IllegalStateException("Response headers have not been sent yet");
        }
        return responseBody;
    }

    public static class HttpCodes {
        public static String RES_200 = "HTTP/1.1 200 OK";
        static String RES_301 = "HTTP/1.1 301 Moved Permanently";
    }
}
