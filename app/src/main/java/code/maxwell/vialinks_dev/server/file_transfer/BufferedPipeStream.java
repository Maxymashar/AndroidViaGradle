package code.maxwell.vialinks_dev.server.file_transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class BufferedPipeStream {
    private final BufferedInputStream dataIn;
    private final BufferedOutputStream dataOut;
    private final int contentLength;
    private final EventStream downloaderEventStream;

    BufferedPipeStream(InputStream dataIn, OutputStream dataOut, int contentLength, EventStream downloadEventStream) {
        this.dataIn = new BufferedInputStream(dataIn, 1024);
        this.dataOut = new BufferedOutputStream(dataOut, 1024);
        this.contentLength = contentLength;
        this.downloaderEventStream = downloadEventStream;
    }

    void transferData() throws IOException {
        int sendBytes = 0;
        while (sendBytes < contentLength) {
            if (!downloaderEventStream.isConnected()) {
                dataOut.flush();
                return;
            }
            dataOut.write(dataIn.read());
            sendBytes++;
        }
        dataOut.flush();
    }
}
