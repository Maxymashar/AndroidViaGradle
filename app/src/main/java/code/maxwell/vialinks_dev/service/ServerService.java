package code.maxwell.vialinks_dev.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.net.InetSocketAddress;

import code.maxwell.vialinks_dev.R;
import code.maxwell.vialinks_dev.server.HttpExchange;
import code.maxwell.vialinks_dev.server.HttpServer;
import code.maxwell.vialinks_dev.server.RequestHandler;
import code.maxwell.vialinks_dev.server.WebServer;
import code.maxwell.vialinks_dev.server.file_transfer.FileTransferServer;

public class ServerService extends Service {
    private static final String TAG = "ServerService";
    Notification notification;
    public static boolean isServerRunning = false;
    public static String uploadIpAddress = null;
    public static String downloadIpAddress = null;
    private HttpServer httpServer = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle("Server Started")
                .setContentText("Server is running ... ")
                .setSmallIcon(R.drawable.ic_via_logo_24px)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final FileTransferServer fileTransferServer = new FileTransferServer();
                httpServer = HttpServer.createServer(new InetSocketAddress(4444));
                httpServer.createContext("/", new RequestHandler() {
                    @Override
                    public void handleRequest(HttpExchange httpExchange) {
                        Log.d(TAG, "handleRequest: >> Request / ");
                        WebServer webServer = new WebServer(ServerService.this);
                        try {
                            webServer.sendRequestedFile(httpExchange);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                httpServer.createContext("/upload_file_data", new RequestHandler() {
                    @Override
                    public void handleRequest(HttpExchange httpExchange) {
                        Log.d(TAG, "handleRequest: >> Request /upload_file_data");
                        fileTransferServer.getUploadedFileData(httpExchange);
                    }
                });
                httpServer.createContext("/download_file_data", new RequestHandler() {
                    @Override
                    public void handleRequest(HttpExchange httpExchange) {
                        Log.d(TAG, "handleRequest: >> Request /download_file_data");
                        fileTransferServer.sendUploadedFileData(httpExchange);
                    }
                });
                httpServer.createContext("/events", new RequestHandler() {
                    @Override
                    public void handleRequest(HttpExchange httpExchange) {
                        Log.d(TAG, "handleRequest: >> Request /events");
                        fileTransferServer.setEventStream(httpExchange);
                    }
                });
                httpServer.createContext("/request_file_upload", new RequestHandler() {
                    @Override
                    public void handleRequest(HttpExchange httpExchange) {
                        Log.d(TAG, "handleRequest: >> Request >> /request_file_upload");
                        fileTransferServer.setPendingRequest(httpExchange);
                    }
                });
                httpServer.createContext("/request_file_download", new RequestHandler() {
                    @Override
                    public void handleRequest(HttpExchange httpExchange) {
                        Log.d(TAG, "handleRequest: >> /request_file_download");
                        fileTransferServer.completePendingRequest(httpExchange);
                    }
                });
                httpServer.createContext("/get_tab_status", new RequestHandler() {
                    @Override
                    public void handleRequest(HttpExchange httpExchange) {
                        Log.d(TAG, "handleRequest: >> Request /get_tab_status");
                        fileTransferServer.getTabStatus(httpExchange);
                    }
                });
                httpServer.setOnBindErrorListener(new HttpServer.OnBindError() {
                    @Override
                    public void onError(final String err) {
                        Log.e(TAG, err);
                    }
                });
                httpServer.setOnBindSuccess(new HttpServer.OnBindSuccess() {
                    @Override
                    public void onSuccess(String msg) {
                        Log.d(TAG, msg);
                        isServerRunning = true;
                    }
                });
                try {
                    httpServer.startServer(); /*Blocking*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        startForeground(101, notification);
        return START_STICKY;
    }
}
