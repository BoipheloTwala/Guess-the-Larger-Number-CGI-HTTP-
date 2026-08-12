import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GuessServer {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/guess", new GuessHandler());
        server.createContext("/bye", new ByeHandler());
        server.setExecutor(null); // default single-threaded executor
        server.start();
        System.out.println("Server running: http://localhost:8000/guess");
    }

    // ---- /guess : shows the question, or judges an answer ----
    static class GuessHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws java.io.IOException {
            Map<String, String> params = parseQueryString(exchange.getRequestURI().getQuery());
            String html = params.containsKey("guessed")
                    ? resultPage(params)
                    : questionPage();
            respond(exchange, html);
        }
    }

    // ---- /bye : exit page ----
    static class ByeHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws java.io.IOException {
            String html = "<html><body><h2>Thanks for playing!</h2>"
                    + "<a href=\"/guess\">Play again</a></body></html>";
            respond(exchange, html);
        }
    }

    private static void respond(HttpExchange exchange, String html) throws java.io.IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        byte[] bytes = html.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String questionPage() {
        Random rnd = new Random();
        int num1 = rnd.nextInt(1000);
        int num2 = rnd.nextInt(1000);
        while (num2 == num1) num2 = rnd.nextInt(1000);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Guess the Larger Number</title>");
        sb.append("<meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\">");
        sb.append("<meta http-equiv=\"Pragma\" content=\"no-cache\">");
        sb.append("<meta http-equiv=\"Expires\" content=\"0\">");
        sb.append("</head><body>");
        sb.append("<h2>Click on the LARGER of the two numbers</h2><p>");
        sb.append("<a href=\"/guess?guessed=" + num1 + "&num1=" + num1 + "&num2=" + num2 + "\">" + num1 + "</a>");
        sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        sb.append("<a href=\"/guess?guessed=" + num2 + "&num1=" + num1 + "&num2=" + num2 + "\">" + num2 + "</a>");
        sb.append("</p></body></html>");
        return sb.toString();
    }

    private static String resultPage(Map<String, String> params) {
        int guessed = Integer.parseInt(params.get("guessed"));
        int num1    = Integer.parseInt(params.get("num1"));
        int num2    = Integer.parseInt(params.get("num2"));
        int larger  = Math.max(num1, num2);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Result</title>");
        sb.append("<meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\">");
        sb.append("<meta http-equiv=\"Pragma\" content=\"no-cache\">");
        sb.append("<meta http-equiv=\"Expires\" content=\"0\">");
        sb.append("</head><body>");
        if (guessed == larger) {
            sb.append("<h2>Correct! " + larger + " is the larger number.</h2><p>Well done.</p>");
        } else {
            sb.append("<h2>Sorry, that's wrong.</h2><p>" + larger + " was the larger number, not " + guessed + ".</p>");
        }
        sb.append("<p><a href=\"/guess\">Try again</a> &nbsp;|&nbsp; <a href=\"/bye\">Quit</a></p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static Map<String, String> parseQueryString(String qs) throws java.io.UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        if (qs == null || qs.isEmpty()) return map;
        for (String pair : qs.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) continue;
            map.put(URLDecoder.decode(pair.substring(0, eq), "UTF-8"),
                     URLDecoder.decode(pair.substring(eq + 1), "UTF-8"));
        }
        return map;
    }
}