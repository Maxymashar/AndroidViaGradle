package code.maxwell.vialinks_dev;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import code.maxwell.vialinks_dev.service.ServerService;
import hotchemi.android.rate.AppRate;

import static code.maxwell.vialinks_dev.service.ServerService.downloadIpAddress;
import static code.maxwell.vialinks_dev.service.ServerService.isServerRunning;
import static code.maxwell.vialinks_dev.service.ServerService.uploadIpAddress;

public class MainActivity extends AppCompatActivity {
    private Button btnStartServer;
    private TextView mTextViewUploadLink;
    private TextView mTextViewDownloadLink;
    private static final String TAG = "MainActivity";
    public static String uploadLinkAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        AppRate.with(this)
                .setInstallDays(0)
                .setLaunchTimes(5)
                .setRemindInterval(1)
                .monitor();
        AppRate.showRateDialogIfMeetsConditions(this);

        btnStartServer = findViewById(R.id.btn_start_server);
        mTextViewUploadLink = findViewById(R.id.text_view_upload_link);
        mTextViewDownloadLink = findViewById(R.id.text_view_download_link);

        /*Set the ipAddress of upload and the download*/
        if (isServerRunning) {
            mTextViewUploadLink.setText(uploadIpAddress);
            mTextViewDownloadLink.setText(downloadIpAddress);
            btnStartServer.setText(R.string.stop_server);
            Log.d(TAG, "onCreate: >> Server is Running");
        } else {
            Log.d(TAG, "onCreate: >> Server not running");
        }

        btnStartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleServerState();
            }
        });
        Log.d(TAG, "onCreate: >> packageName >> " + getPackageName());

    }

    private String getIpAddress() {
        try {
            Enumeration<NetworkInterface> enumNetworkInterface = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterface.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterface.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        String ipAddress = inetAddress.getHostAddress();
                        if (ipAddress.startsWith("192.168.43")) {
                            return ipAddress;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error in getting IPAddress", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private void toggleServerState() {
        if (!isServerRunning) {
            startServer();
        } else {
            stopServer();
        }
    }

    private void stopServer() {
        Intent intent = new Intent(MainActivity.this, ServerService.class);
        intent.setAction("stop");
        stopService(intent);
        btnStartServer.setText(R.string.start_server);
        Toast.makeText(this, "Server stopped", Toast.LENGTH_SHORT).show();
        isServerRunning = false;
    }

    private void startServer() {
        String ip = getIpAddress();
        if (ip == null) {
            Toast.makeText(MainActivity.this, "Turn on wifi hotspot first", Toast.LENGTH_SHORT).show();
            return;
        }

        String uploadLink = ip + ":4444/upload.html";
        String downloadLink = ip + ":4444/download.html";
        mTextViewUploadLink.setText(uploadLink);
        mTextViewDownloadLink.setText(downloadLink);
        uploadLinkAddress = uploadLink;
        uploadIpAddress = uploadLink;
        downloadIpAddress = downloadLink;

        Intent intent = new Intent(MainActivity.this, ServerService.class);
        startService(intent);
        Toast.makeText(MainActivity.this, "Server started", Toast.LENGTH_SHORT).show();
        btnStartServer.setText(R.string.stop_server);
        isServerRunning = true;
    }

}

