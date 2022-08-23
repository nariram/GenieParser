package com.genie.parser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.File;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


class App implements HttpHandler {
    public static void main() throws Exception { //Pass File Names for Read and Write
    //public static void main(String[] args) throws Exception { //Pass File Names for Read and Write

        //System.out.println(args[0]);
        //System.out.println(args[1]);
    	
    	//String file_name = "../com.genie/src/main/resources/showversion.txt";
    	//String file2 = "../com.genie/src/main/resources/showversion.json";
    	
    	String file_name = "output.txt";
    	String file2 = "parser.json";

        //Initialize File Writer for Python File
        FileWriter py = new FileWriter("parser.py");

        //Initialize Scanner to read Command Output
        //File fr = new File(args[0]);
        File fr = new File(file_name);
        Scanner myReader = new Scanner(fr);
        
        int currLineNum = 1;

        //Python Imports
        py.write("import re\n");
        py.write("import json\n\n");

        //Convert JSON to Java Object
        Gson gson = new Gson();
        //JsonReader reader = new JsonReader(new FileReader(args[1]));
        JsonReader reader = new JsonReader(new FileReader(file2));
        Line[] lines = gson.fromJson(reader, Line[].class);
        
        //Create Regular Expression Dictionary
        py.write("rx_dict =  {\n");
        for (int i = 0; i < lines.length; i++) {
            Line currLine = lines[i];
            String lineText = new String();
            //Get the cooresponding line in the command output
            while (currLineNum <= currLine.lineNum) {
                lineText = myReader.nextLine(); //String form of current line
                currLineNum++;
            }

            //Length of the Current Line
            int lineLength = lineText.length();
            
            //Mark Spaces occupied by Variables in HashMap blocked
            HashMap<Integer, Boolean> blocked = new HashMap<Integer, Boolean>();
            List<Selection> lineSelections = currLine.selections;
            for (int j = 0; j < lineSelections.size(); j++) {
                for (int k = lineSelections.get(j).start; k <= lineSelections.get(j).end; k++) {
                    blocked.put(k, true);
                }
            }
            
            //Traverse Line and Create RegExp
            String lineExp = new String("re.compile(r\"^");
            int currSelection = 0;
            for (int j = 0; j < lineLength; j++) {
                if (!blocked.containsKey(j)) {
                    if (lineText.charAt(j) == '^' || lineText.charAt(j) == '$' || lineText.charAt(j) == '.'
                            || lineText.charAt(j) == '|' || lineText.charAt(j) == '+' || lineText.charAt(j) == '*'
                            || lineText.charAt(j) == '?'
                            || lineText.charAt(j) == '[' || lineText.charAt(j) == ']' || lineText.charAt(j) == '(' || lineText.charAt(j) == ')' ) {
                        lineExp += ("\\" + lineText.charAt(j));
                    }
                    else if (lineText.charAt(j) == '\\') {
                        lineExp += ("\\\\");
                    } 
                    else {
                        lineExp += lineText.charAt(j);
                    } 
                } else if (blocked.get(j) == true) {
                    lineExp += "(?P<" + lineSelections.get(currSelection).name + ">.*)";
                    j = lineSelections.get(currSelection).end;
                    currSelection++;
                }
            }
            lineExp += "$\"),";
            py.write("  \"r" + String.valueOf(currLine.lineNum) + "\"" + ": " + lineExp + "\n");
        }

        //Close Regular Expression Dictionary
        py.write("}\n\n");

        //Python Code for Parsing File
        py.write("def parse_file(filepath):\n");
        py.write("  data = []\n");
        //Iterate through rx_dict and store all keys in a new List
        py.write("  with open(filepath, \"r\") as file_object:\n");
        py.write("      line = file_object.readline()\n");
        py.write("      while line:\n");
        py.write("          for key, rx in rx_dict.items():\n");
        py.write("              if rx.search(line):\n");
        py.write("                  for m in rx.finditer(line):\n");
        py.write("                      data.append(m.groupdict())\n");
        py.write("          line = file_object.readline()\n");     
        py.write("  return data\n\n");


        //Python Driver Code
        py.write("if __name__ == \"__main__\":\n");
        py.write("  filepath = \"");
        //py.write(args[0]);
        py.write(file_name);
        py.write("\"\n");
        
        py.write("  data = parse_file(filepath)\n");
        py.write("  print(\"File Data:\", data)\n");
        py.write("  with open('data.json', 'w') as f:\n");
        py.write("      json.dump(data, f)\n");


        //Close Scanner
        myReader.close();

        //Close File Writer
        py.close();
    }

	public void handle(HttpExchange exchange) throws IOException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		URI requestedUri = exchange.getRequestURI();
		System.out.println("\n URL: " + requestedUri + "\n");
		String query = requestedUri.getRawQuery();
		System.out.println("\n Query: " + query + "\n");
	  
		InputStreamReader in_strm = new InputStreamReader(exchange.getRequestBody(), "utf-8");
		BufferedReader br = new BufferedReader(in_strm);
	        
	    String data = "";
        String line;
        while ((line = br.readLine()) != null) {
  	        data += line + "\n";
        }        
    	Pattern pattern_for_text_content = Pattern.compile("Content-Type: text/plain",
    			Pattern.CASE_INSENSITIVE);
        System.out.println("Pattern : "+ pattern_for_text_content);
        String[] result = pattern_for_text_content.split(data);
        
        Pattern pattern_for_for_json_content = Pattern.compile("Content-Type: application/json",
    			Pattern.CASE_INSENSITIVE);
        System.out.println("Pattern : "+ pattern_for_for_json_content);
        String[] results = pattern_for_for_json_content.split(result[1]);
        
        System.out.println("Text File content:" + results[0]);
        FileWriter txt_obj = new FileWriter("output.txt");
        txt_obj.write(results[0]);
        txt_obj.close();
        
        System.out.println("JSON File content:" + results[1]);
        FileWriter json_obj = new FileWriter("parser.json");
        json_obj.write(results[1]);
        json_obj.close();

		//Call main function
		System.out.println("\n Call Main Method\n");
		
		try {
			main();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		/*
        File file = new File("parser.py");
        String response = FileUtils.readFileToString(file);
        System.out.println("Response: " + response + "\n");
        
        String encoding = "UTF-8";
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=" + encoding);
        exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes("UTF-8"));
        os.close();
        */


        //Open a file read data and append to response
        File file = new File("parser.py");

	    String response = "";
        try (BufferedReader br1 = new BufferedReader(new FileReader(file)))
        {
            String line1;
            while ((line1 = br1.readLine()) != null) {
                System.out.println(line1);
  	            response += line1 + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Response: " + response + "\n");
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        
        os.write(response.toString().getBytes());
        os.close();
		
	}
}

