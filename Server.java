package com.genie.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

// Same package since not required to import the App library
//import com.genie.parser.App;


public class Server {

  public static void main(String[] args) throws Exception {
      
      int port = 9000;
      HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
      System.out.println("server started at " + port);
      
      server.createContext("/", new RootHandler());
      server.createContext("/hello", new HelloHandler());
      server.createContext("/echoHeader", new EchoHeaderHandler());
      
      server.createContext("/parser", new App());
            
      server.setExecutor(null);
      server.start();
  }
}


// NOTE: This Class used for testing purpose only, will be removed when we deploy the server
class HelloHandler implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {
        String response = "Hello World";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

//NOTE: This Class used for testing purpose only, will be removed when we deploy the server
class RootHandler implements HttpHandler {
  public void handle(HttpExchange he) throws IOException {
      String response = "Server start success if you see this message";
      he.sendResponseHeaders(200, response.length());
      OutputStream os = he.getResponseBody();
      os.write(response.getBytes());
      os.close();
  }
}

//NOTE: This Class used for testing purpose only, will be removed when we deploy the server
class EchoHeaderHandler implements HttpHandler {
  public void handle(HttpExchange he) throws IOException {
      Headers headers = he.getRequestHeaders();
      System.out.println("\n Initial Mappings are: " + headers.entrySet()+ "\n");
      Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
      String response = "";
      for (Map.Entry<String, List<String>> entry : entries) {
    	  //System.out.println("Initial Mappings are: " + entry.toString());
          response += entry.toString() + "\n";
      }
      he.sendResponseHeaders(200, response.length());
      OutputStream os = he.getResponseBody();
      os.write(response.toString().getBytes());
      os.close();
  }
}


