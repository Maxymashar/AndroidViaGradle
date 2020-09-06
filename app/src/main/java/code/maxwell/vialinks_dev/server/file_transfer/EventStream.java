package code.maxwell.vialinks_dev.server.file_transfer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import code.maxwell.vialinks_dev.server.HttpExchange;

class EventStream {
    private final HttpExchange httpExchange;
    private final BufferedWriter eventWriter;
    private boolean isConnected = true;

    EventStream(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        sendHeaders();
        eventWriter = new BufferedWriter(new OutputStreamWriter(httpExchange.getResponseBody()));
        sendPingEvents();
    }

    private void sendHeaders() {
        httpExchange.getResponseHeaders().put("Content-Type", "text/event-stream");
        httpExchange.sendResponseHeaders(HttpExchange.HttpCodes.RES_200, -1);
    }

    void sendEvent(String event, String data) throws IOException {
        eventWriter.write("event: " + event + "\n");
        eventWriter.write("data: " + data + "\n");
        eventWriter.write("\n\n");
        eventWriter.flush();
    }

    boolean isConnected() {
        return isConnected;
    }

    private void sendPingEvents() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!isConnected) {
                        break;
                    }
                    try {
                        sendEvent("ping", "pong");
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                        break;
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        isConnected = false;
                    }
                }
            }
        }).start();
    }
}
