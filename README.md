## Guess the Larger Number

A dynamic, server-driven number-guessing game built as CGI / raw HTTP, no client-side JavaScript, no frameworks.

## How it works
The server generates two random numbers and displays each as a hyperlink.
Clicking the larger number returns a "Correct!" page; clicking the smaller one returns a "Wrong" page.
Both numbers and the guess are round-tripped through the URL's query string, so a single script/handler both asks the question and judges the answer, no server-side session state required.
Cache-Control / Pragma / Expires meta tags stop the browser from reusing a stale (cached) round.
Two versions included
CGI version - reads QUERY_STRING / SCRIPT_NAME environment variables, intended to run under a real CGI-capable web server (e.g. Apache mod_cgi).
Self-contained version - uses the JDK's built-in com.sun.net.httpserver.HttpServer, so it can be compiled and run directly with no external server setup (useful for local testing).

## Running (self-contained version)
bash

javac GuessServer.java

java GuessServer

Then open http://localhost:8000/guess in a browser.

## Known limitation

Hovering over a number link reveals the target URL (which contains both numbers), so a determined user could avoid actually comparing them. Noted and accepted per the assignment brief — the "real" fix would be storing the two numbers server-side against a random session token instead of passing them in the URL.
