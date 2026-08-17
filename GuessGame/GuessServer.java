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
            String html = byePage();
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

    private static String sharedHead(String title) {
        return "<!DOCTYPE html><html lang=\"en\"><head>"
            + "<meta charset=\"UTF-8\">"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
            + "<meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\">"
            + "<meta http-equiv=\"Pragma\" content=\"no-cache\">"
            + "<meta http-equiv=\"Expires\" content=\"0\">"
            + "<title>" + title + "</title>"
            + "<style>"
            + "*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }"
            + "body {"
            + "  font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;"
            + "  min-height: 100vh;"
            + "  display: flex; align-items: center; justify-content: center;"
            + "  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);"
            + "  color: #e0e0e0;"
            + "}"
            + ".card {"
            + "  background: rgba(255,255,255,0.07);"
            + "  backdrop-filter: blur(12px);"
            + "  border: 1px solid rgba(255,255,255,0.12);"
            + "  border-radius: 24px;"
            + "  padding: 48px 56px;"
            + "  text-align: center;"
            + "  box-shadow: 0 20px 60px rgba(0,0,0,0.4);"
            + "  max-width: 520px; width: 90%;"
            + "}"
            + ".subtitle {"
            + "  font-size: 0.85rem;"
            + "  letter-spacing: 0.2em;"
            + "  text-transform: uppercase;"
            + "  color: #a78bfa;"
            + "  margin-bottom: 10px;"
            + "}"
            + "h1 { font-size: 1.9rem; font-weight: 700; margin-bottom: 36px; line-height: 1.3; }"
            + ".numbers { display: flex; gap: 24px; justify-content: center; margin-bottom: 36px; }"
            + ".num-btn {"
            + "  display: flex; align-items: center; justify-content: center;"
            + "  width: 140px; height: 140px;"
            + "  border-radius: 20px;"
            + "  background: linear-gradient(145deg, #5b21b6, #7c3aed);"
            + "  color: #fff;"
            + "  font-size: 2.6rem; font-weight: 800;"
            + "  text-decoration: none;"
            + "  transition: transform 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;"
            + "  box-shadow: 0 8px 24px rgba(124,58,237,0.45);"
            + "  user-select: none;"
            + "}"
            + ".num-btn:hover {"
            + "  transform: translateY(-6px) scale(1.06);"
            + "  box-shadow: 0 16px 40px rgba(124,58,237,0.65);"
            + "  background: linear-gradient(145deg, #6d28d9, #8b5cf6);"
            + "}"
            + ".num-btn:active { transform: scale(0.97); }"
            + ".result-icon { font-size: 4rem; margin-bottom: 16px; }"
            + ".result-correct h1 { color: #34d399; }"
            + ".result-wrong h1 { color: #f87171; }"
            + ".detail { font-size: 1rem; color: #94a3b8; margin-bottom: 32px; line-height: 1.6; }"
            + ".detail span { color: #e2e8f0; font-weight: 600; }"
            + ".actions { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }"
            + ".btn {"
            + "  padding: 12px 28px;"
            + "  border-radius: 12px;"
            + "  font-size: 0.95rem; font-weight: 600;"
            + "  text-decoration: none;"
            + "  transition: transform 0.12s ease, box-shadow 0.12s ease;"
            + "}"
            + ".btn-primary {"
            + "  background: linear-gradient(135deg, #7c3aed, #5b21b6);"
            + "  color: #fff;"
            + "  box-shadow: 0 4px 16px rgba(124,58,237,0.4);"
            + "}"
            + ".btn-primary:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(124,58,237,0.55); }"
            + ".btn-secondary {"
            + "  background: rgba(255,255,255,0.08);"
            + "  color: #cbd5e1;"
            + "  border: 1px solid rgba(255,255,255,0.15);"
            + "}"
            + ".btn-secondary:hover { background: rgba(255,255,255,0.14); transform: translateY(-2px); }"
            + ".bye-icon { font-size: 4rem; margin-bottom: 16px; }"
            + "</style>"
            + "</head><body>";
    }

    private static String questionPage() {
        Random rnd = new Random();
        int num1 = rnd.nextInt(1000);
        int num2 = rnd.nextInt(1000);
        while (num2 == num1) num2 = rnd.nextInt(1000);

        return sharedHead("Guess the Larger Number")
            + "<div class=\"card\">"
            + "  <p class=\"subtitle\">Number Challenge</p>"
            + "  <h1>Which number is larger?</h1>"
            + "  <div class=\"numbers\">"
            + "    <a class=\"num-btn\" href=\"/guess?guessed=" + num1 + "&num1=" + num1 + "&num2=" + num2 + "\">" + num1 + "</a>"
            + "    <a class=\"num-btn\" href=\"/guess?guessed=" + num2 + "&num1=" + num1 + "&num2=" + num2 + "\">" + num2 + "</a>"
            + "  </div>"
            + "  <p class=\"detail\">Tap the card with the bigger value</p>"
            + "</div>"
            + "</body></html>";
    }

    private static String resultPage(Map<String, String> params) {
        int guessed = Integer.parseInt(params.get("guessed"));
        int num1    = Integer.parseInt(params.get("num1"));
        int num2    = Integer.parseInt(params.get("num2"));
        int larger  = Math.max(num1, num2);
        boolean correct = guessed == larger;

        String icon    = correct ? "&#127881;" : "&#128532;";
        String cssClass = correct ? "result-correct" : "result-wrong";
        String heading  = correct
            ? "Correct! " + larger + " is bigger."
            : "Not quite!";
        String detail   = correct
            ? "Great instincts — you picked the right number."
            : "<span>" + larger + "</span> was the larger number, not <span>" + guessed + "</span>.";

        return sharedHead("Result")
            + "<div class=\"card " + cssClass + "\">"
            + "  <div class=\"result-icon\">" + icon + "</div>"
            + "  <h1>" + heading + "</h1>"
            + "  <p class=\"detail\">" + detail + "</p>"
            + "  <div class=\"actions\">"
            + "    <a class=\"btn btn-primary\" href=\"/guess\">Play again</a>"
            + "    <a class=\"btn btn-secondary\" href=\"/bye\">Quit</a>"
            + "  </div>"
            + "</div>"
            + "</body></html>";
    }

    private static String byePage() {
        return sharedHead("Thanks for Playing!")
            + "<div class=\"card\">"
            + "  <div class=\"bye-icon\">&#128075;</div>"
            + "  <h1>Thanks for playing!</h1>"
            + "  <p class=\"detail\">Hope you had fun. Come back any time.</p>"
            + "  <div class=\"actions\">"
            + "    <a class=\"btn btn-primary\" href=\"/guess\">Play again</a>"
            + "  </div>"
            + "</div>"
            + "</body></html>";
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