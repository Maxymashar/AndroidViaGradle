package code.maxwell.vialinks_dev.server.file_transfer;

import code.maxwell.vialinks_dev.server.HttpExchange;

class PendingRequest {
    private final HttpExchange httpExchange;

    PendingRequest(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;

    }

    HttpExchange getHttpExchange() {
        return httpExchange;
    }
}
