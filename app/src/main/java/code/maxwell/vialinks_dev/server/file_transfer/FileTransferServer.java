package code.maxwell.vialinks_dev.server.file_transfer;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import code.maxwell.vialinks_dev.server.HttpExchange;

import static code.maxwell.vialinks_dev.server.WebServer.getMimeType;
import static java.nio.charset.StandardCharsets.UTF_8;

public class FileTransferServer {
    private static final String TAG = "FileTransferServer";
    private static ArrayList<UpdateRequest> updateRequestArrayList = new ArrayList<>();
    private static PendingRequest pendingRequest = null;
    private static EventStream uploadEventStream = null;
    private static EventStream downloadEventStream = null;

    public void getUploadedFileData(HttpExchange httpExchange) {
        try {
            InputStream dataIn = httpExchange.getRequestBody();
            Log.d(TAG, "getUploadedFileData: >> Requested for dataIn");
            StringBuilder sb = new StringBuilder();
            int contentLength = Integer.parseInt(Objects.requireNonNull(httpExchange.getRequestHeaders().get("Content-Length")).trim());
            int count = 1;
            while (count <= contentLength) {
                int readByte = dataIn.read();
                sb.append((char) readByte);
                count++;
            }

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<UpdateRequest>>() {
            }.getType();
            ArrayList<UpdateRequest> newUpdateRequests = gson.fromJson(sb.toString(), type);
            updateRequestArrayList.addAll(newUpdateRequests);
            // send the data to the downloader if the downloader is live
            if (downloadEventStream != null && downloadEventStream.isConnected()) {
                try {
                    downloadEventStream.sendEvent("update_list", gson.toJson(newUpdateRequests));
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
            Log.d(TAG, "getUploadedFileData: >> UpdateRequest >> " + sb.toString());
            httpExchange.getResponseHeaders().put("Content-Type", getMimeType(".json"));
            httpExchange.sendResponseHeaders(HttpExchange.HttpCodes.RES_200, sb.toString().getBytes().length);
            OutputStream dataOut = httpExchange.getResponseBody();
            dataOut.write(sb.toString().getBytes());
            dataOut.flush();

            dataOut.close();
            dataIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUploadedFileData(HttpExchange httpExchange) {

        try {
            if (uploadEventStream != null && !uploadEventStream.isConnected()) {
                updateRequestArrayList.clear();
            }
            Gson gson = new Gson();
            String response = gson.toJson(updateRequestArrayList);

//          Send the response headers
            httpExchange.getResponseHeaders().put("Content-Type", getMimeType(".json"));
            httpExchange.sendResponseHeaders(HttpExchange.HttpCodes.RES_200, response.getBytes().length);

            OutputStream dataOut = httpExchange.getResponseBody();
            dataOut.write(response.getBytes());
            dataOut.flush();

            httpExchange.getRequestBody().close();
            dataOut.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    public void setEventStream(HttpExchange httpsExchange) {
        String[] path = httpsExchange.getRequestURIPath().split("/");
        Log.d(TAG, "setEventStream: >> SetEventStream Path >> " + httpsExchange.getRequestURIPath());
        String eventClient = path[2];
        if (eventClient.equals("upload")) {
            if (uploadEventStream != null) {
                try {
                    uploadEventStream.sendEvent("close_connection", "null");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                uploadEventStream = null;
            }
            uploadEventStream = new EventStream(httpsExchange);
            Log.d(TAG, "setEventStream: Added new upload event stream");
            updateRequestArrayList.clear();
            if (downloadEventStream != null) {
                try {
                    downloadEventStream.sendEvent("update_list", "[{\"file_name\":\"null\",\"file_size\":\"null\",\"action\":\"delete_all\"}]");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (downloadEventStream != null) {
                try {
                    downloadEventStream.sendEvent("close_connection_", "null");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                downloadEventStream = null;
            }
            downloadEventStream = new EventStream(httpsExchange);
            Log.d(TAG, "setEventStream: Added new download event stream");
        }
    }

    public void setPendingRequest(HttpExchange httpExchange) {
        String[] path = httpExchange.getRequestURIPath().split("/");
        System.out.println("Received Download Path >> " + httpExchange.getRequestURIPath());
        String requestedFile = path[2];
        String requestId = path[3];
        System.out.println("Received upload request of the file >> " + requestedFile + " of file Id >> " + requestId);
        pendingRequest = new PendingRequest(httpExchange);

        /*forward the file-upload-request to the uploader*/
        try {
            uploadEventStream.sendEvent("upload_file", "{\"uuid\":\"" + requestedFile + "\",\"request_id\":\"" + requestId + "\"}");
        } catch (IOException | NullPointerException e) {
            System.err.println(e.getMessage());
            try {
                httpExchange.getRequestBody().close();
                httpExchange.getResponseBody().close();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
            pendingRequest = null;
        }
    }

    private void sendFile(HttpExchange upload, HttpExchange download) throws IOException {
        /*Send the response headers*/
        /*1) To the downloader*/
        HashMap<String, String> uploadRequestHeaders = upload.getRequestHeaders();
        String uploadedFileName = URLDecoder.decode(uploadRequestHeaders.get("FileName"), UTF_8.name());
        String contentLength = uploadRequestHeaders.get("Content-Length");

        HashMap<String, String> downloadResponseHeaders = download.getResponseHeaders();
        downloadResponseHeaders.put("Content-Disposition", "attachment; filename=\"" + uploadedFileName + "\"");
        assert contentLength != null;
        download.sendResponseHeaders(HttpExchange.HttpCodes.RES_200, Integer.parseInt(contentLength.trim()));

        /*Send the response headers to the uploader*/
        String response = "{\"file_name\":\"" + uploadedFileName + "\",\"content-length\":\"" + contentLength + "\"}";
        HashMap<String, String> uploadResponseHeaders = upload.getResponseHeaders();
        uploadResponseHeaders.put("Content-Type", getMimeType(".json"));
        upload.sendResponseHeaders(HttpExchange.HttpCodes.RES_200, response.getBytes().length);

        BufferedWriter responseWriter = new BufferedWriter(new OutputStreamWriter(upload.getResponseBody()));
        responseWriter.write(response);
        responseWriter.flush();

        /*Send the file to the downloader*/
        InputStream dataIn = upload.getRequestBody();
        OutputStream dataOut = download.getResponseBody();
        BufferedPipeStream bufferedPipeStream = new BufferedPipeStream(dataIn, dataOut, Integer.parseInt(contentLength.trim()), downloadEventStream);
        bufferedPipeStream.transferData();
        System.out.println("Successfully Transferred file >> " + uploadedFileName + " of length >> " + contentLength + " bytes");
    }

    public void completePendingRequest(HttpExchange httpExchange) {
        if (pendingRequest != null) {
            try {
                HttpExchange pendingHttpExchange = pendingRequest.getHttpExchange();
                try {
                    sendFile(httpExchange, pendingHttpExchange);
                } catch (IOException e) {
                    /*The download or the upload was cancelled*/
                    String requestedFile = pendingHttpExchange.getRequestURIPath().split("/")[2].trim();
                    System.err.println("There was an error with  transfer of the file " + requestedFile);
                }
                /*Close the Connections*/
                try {
                    httpExchange.getRequestBody().close();
                } catch (IOException e) {
                    System.err.println("Connection closed when there was an ongoing download >> " + e.getMessage());
                }
                httpExchange.getResponseBody().close();
                pendingHttpExchange.getRequestBody().close();
                try {
                    pendingHttpExchange.getResponseBody().close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
                pendingRequest = null;

                /*Send the request-next-file request*/
                HashMap<String, String> uploadRequestHeaders = httpExchange.getRequestHeaders();
                String uploadedFileName = Objects.requireNonNull(URLDecoder.decode(uploadRequestHeaders.get("FileName"),UTF_8.name())).trim();
                String requestId = Objects.requireNonNull(uploadRequestHeaders.get("RequestId")).trim();
                String contentLength = Objects.requireNonNull(uploadRequestHeaders.get("Content-Length")).trim();
                String uuid = Objects.requireNonNull(uploadRequestHeaders.get("uuid")).trim();

                String message = "{\"comp_file_name\":\"" + uploadedFileName + "\",\"comp_request_id\":\"" + requestId + "\",\"comp_file_size\":\"" + contentLength + "\",\"uuid\":\"" + uuid + "\"}";
                Log.d(TAG, "completePendingRequest: >> message >> " + message);
                try {
                    downloadEventStream.sendEvent("download_completed", message);
                } catch (IOException | NullPointerException e) {
                    System.err.println(e.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getTabStatus(HttpExchange httpExchange) {
        /*An Ajax request to check if there are any open tabs*/
        String clientType = httpExchange.getRequestURIPath().split("/")[2];
        String responseJSON = "";
        if (clientType.equals("upload")) {
            if (uploadEventStream != null) {
                if (uploadEventStream.isConnected()) {
                    //language=JSON
                    responseJSON = "{\"is_open\":" + true + "}";
                } else {
                    //language=JSON
                    responseJSON = "{\"is_open\":" + false + "}";
                    updateRequestArrayList.clear();
                }
            } else {
                //language=JSON
                responseJSON = "{\"is_open\":" + false + "}";
                updateRequestArrayList.clear();
            }
        } else if (clientType.equals("download")) {
            if (downloadEventStream != null) {
                if (downloadEventStream.isConnected()) {
                    //language=JSON
                    responseJSON = "{\"is_open\":" + true + "}";
                } else {
                    //language=JSON
                    responseJSON = "{\"is_open\":" + false + "}";
                }
            } else {
                //language=JSON
                responseJSON = "{\"is_open\":" + false + "}";
            }
        }
        httpExchange.getResponseHeaders().put("Content-Type", getMimeType(".json"));
        httpExchange.sendResponseHeaders(HttpExchange.HttpCodes.RES_200, responseJSON.getBytes().length);
        BufferedWriter respWriter = new BufferedWriter(new OutputStreamWriter(httpExchange.getResponseBody()));
        try {
            respWriter.write(responseJSON);
            respWriter.flush();
            httpExchange.getRequestBody().close();
            httpExchange.getResponseBody().close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
