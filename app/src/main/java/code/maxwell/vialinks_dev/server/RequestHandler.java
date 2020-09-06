package code.maxwell.vialinks_dev.server;

public interface RequestHandler {
    void handleRequest(HttpExchange httpExchange);
}
