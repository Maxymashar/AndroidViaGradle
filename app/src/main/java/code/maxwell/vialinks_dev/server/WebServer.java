package code.maxwell.vialinks_dev.server;

import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import code.maxwell.vialinks_dev.MainActivity;

public class WebServer {
    private Context context;
    private static final String TAG = "WebServer";

    public WebServer(Context context) {
        this.context = context;
    }

    private String getRequestedFileName(String requestedPath) {
        if (requestedPath.equals("/")) {
            return requestedPath;
        } else {
            return "web" + requestedPath;
        }
    }

    private int getFileSize(String filename) {
        try {
            InputStream inputStream = context.getAssets().open(filename);
            int size = inputStream.available();
            inputStream.close();
            return size;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private byte[] getFileData(String filename) {
        byte[] buffer = new byte[getFileSize(filename)];
        try {
            InputStream inputStream = context.getAssets().open(filename);
            int readBytes = inputStream.read(buffer);
            Log.d(TAG, "getFileData: >> Read " + readBytes + " from the file " + filename);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static String getMimeType(String filename) {
        if (filename.endsWith(".html")) {
            return "text/html";
        } else if (filename.endsWith(".css")) {
            return "text/css";
        } else if (filename.endsWith(".js")) {
            return "text/javascript";
        } else if (filename.endsWith(".json")) {
            return "application/json";
        } else if (filename.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (filename.endsWith(".ico")) {
            return "image/vnd.microsoft.icon";
        } else if (filename.equals("event_stream")) {
            return "text/event-stream";
        }
        return "";
    }

    public void sendRequestedFile(HttpExchange httpExchange) throws IOException {
        String requestedPath = httpExchange.getRequestURIPath();
        String requestedFilename = getRequestedFileName(requestedPath);
        Log.d(TAG, "sendRequestedFile: >> RequestedFile >> " + requestedFilename + " of mime-type " + getMimeType(requestedFilename));

        if (requestedFilename.equals("/")) {
            httpExchange.getResponseHeaders().put("Location", "http://"+MainActivity.uploadLinkAddress);
            httpExchange.sendResponseHeaders(HttpExchange.HttpCodes.RES_301, 0);
            httpExchange.getRequestBody().close();
            httpExchange.getResponseBody().close();
            return;
        }
        /*Send the response headers*/
        HashMap<String, String> responseHeaders = httpExchange.getResponseHeaders();
        responseHeaders.put("Content-Type", getMimeType(requestedFilename));
        httpExchange.sendResponseHeaders(HttpExchange.HttpCodes.RES_200, getFileSize(requestedFilename));

        /*Send the requested file*/
        InputStream dataIn = httpExchange.getRequestBody();
        OutputStream dataOut = httpExchange.getResponseBody();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(dataOut);
        bufferedOutputStream.write(getFileData(requestedFilename));
        bufferedOutputStream.flush();

        /*Close the Streams*/
        dataIn.close();
        dataOut.close();
    }
}
