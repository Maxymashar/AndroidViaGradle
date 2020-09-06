package code.maxwell.vialinks_dev.server;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;

import static code.maxwell.vialinks_dev.service.ServerService.isServerRunning;

public class HttpServer {
    private static final String TAG = "HttpServer";
    private static HttpServer httpServer = null;
    private static InetSocketAddress inetSocketAddress;
    private HashMap<String, RequestHandler> registeredRequests = new HashMap<>();
    private OnBindError onBindError = null;
    private OnBindSuccess onBindSuccess = null;

    private HttpServer(InetSocketAddress inetSocketAddress) {
        HttpServer.inetSocketAddress = inetSocketAddress;
    }

    public static HttpServer createServer(InetSocketAddress socketAddress) {
        if (httpServer == null) {
            httpServer = new HttpServer(socketAddress);
        }
        return httpServer;
    }

    /*Adds the context to the array of the registeredRequests*/
    public void createContext(String uniquePath, RequestHandler requestHandler) {
        registeredRequests.put(uniquePath, requestHandler);
    }

    public void setOnBindErrorListener(OnBindError onBindErrorListener) {
        this.onBindError = onBindErrorListener;
    }

    public void setOnBindSuccess(OnBindSuccess onBindSuccessListener) {
        this.onBindSuccess = onBindSuccessListener;
    }


    public void startServer() throws IOException {
        final ServerSocket mServerSocket;
        try {
            mServerSocket = new ServerSocket(inetSocketAddress.getPort());
            onBindSuccess.onSuccess("Success binding to the port 4444");
            Log.d(TAG, "startServer: >> Server started ... Listening @port : " + mServerSocket.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
            onBindError.onError("Error binding to the port 4444");
            return;
        }
        while (true) {
            final Socket mSocket = mServerSocket.accept(); /*Blocking*/
            if (!isServerRunning) {
                if (mServerSocket.isBound()) {
                    mServerSocket.close();
                    Log.d(TAG, "startServer: >> closed the ServerSocket");
                }
                if (mSocket.isBound()) {
                    mSocket.close();
                    Log.d(TAG, "startServer: >> closed the socket");
                }
                break;
            }
            Log.d(TAG, "startServer: >> Server connected to client : " + mSocket.getRemoteSocketAddress());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream inputStream = mSocket.getInputStream();
                        OutputStream outputStream = mSocket.getOutputStream();

                        HttpExchange httpExchange = new HttpExchange();
                        httpExchange.setRequestBody(inputStream);
                        httpExchange.setResponseBody(outputStream);

                        /*Get the headers*/
                        setRequestHttpHeaders(httpExchange);

                        /*Get the requestPath*/
                        String requestedPath = httpExchange.getRequestURIPath();
                        if (requestedPath == null) {
                            return;
                        }
                        if (requestedPath.split("/").length == 0) {
                            if (registeredRequests.containsKey("/")) {
                                Objects.requireNonNull(registeredRequests.get("/")).handleRequest(httpExchange);
                            } else {
                                throw new RuntimeException("No registered context for \"/\"");
                            }
                        } else {
                            Log.d(TAG, "run: >> RequestedPath >> " + requestedPath);
                            String[] splitPath = requestedPath.split("/");
                            String testPath = "/" + splitPath[1].trim();
                            if (registeredRequests.containsKey(testPath)) {
                                Objects.requireNonNull(registeredRequests.get(testPath)).handleRequest(httpExchange);
                            } else {
                                Log.e(TAG, "run: handleRequest >> ", new Throwable("The requestedPath >> " + testPath + " is not registered"));
                                if (registeredRequests.containsKey("/")) {
                                    Objects.requireNonNull(registeredRequests.get("/")).handleRequest(httpExchange);
                                } else {
                                    throw new RuntimeException("No registered context for \"/\"");
                                }
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private String readLine(InputStream dataIn) throws IOException {
        boolean isFirstLine = true;
        StringBuilder sb = new StringBuilder();
        while (true) {
            int readByte = dataIn.read();
            if (readByte == -1) {
                if (isFirstLine) {
                    return null;
                } else {
                    return sb.toString();
                }
            } else if (readByte == 10) {
                return sb.toString();
            }
            sb.append((char) readByte);
            isFirstLine = false;
        }
    }

    private void setRequestHttpHeaders(HttpExchange httpExchange) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        HashMap<String, String> headers = httpExchange.getRequestHeaders();

        while (true) {
            String line = readLine(inputStream);
            if (line != null) {
                if (line.trim().length() == 0) {
                    break;
                }
                if (line.startsWith("GET") || line.startsWith("POST")) {
                    String requestedPath = line.split(" ")[1];
                    httpExchange.setRequestURIPath(requestedPath);
                } else {
                    String key = line.split(":")[0];
                    String value = line.split(":")[1];
                    headers.put(key, value);
                }
            }
        }
    }

    public interface OnBindError {
        void onError(String err);
    }

    public interface OnBindSuccess {
        void onSuccess(String msg);
    }
}
