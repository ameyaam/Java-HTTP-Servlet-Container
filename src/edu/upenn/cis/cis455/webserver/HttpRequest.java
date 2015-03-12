package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import javax.servlet.http.Cookie;

public class HttpRequest {

	public HashMap<String, String> headerValueMap;
	Cookie[] cookie;
	String httpMethod;
	String relativePath;
	String servletUrl;
	String queryString = null;
	BufferedReader inFromClient;
	String httpVersion;
	int port;
	Socket socket;
	public HttpRequest(BufferedReader in, String m, String v, int p, Socket s) throws Exception
	{
		socket = s;
		port = p;
		httpVersion = v;
		inFromClient = in;
		headerValueMap = parseHttpHeaders(in);
		//System.out.println("Map yo: " + headerValueMap);
		httpMethod = m;

		if (headerValueMap.get("cookie") != null)
		{
			String cookieString = headerValueMap.get("cookie");
			cookie = parseRawCookie(cookieString);
		}
	}
	
	Cookie[] parseRawCookie(String rawCookie) throws Exception {
		
		String[] nameValuePairs = rawCookie.split(";");
		int numOfCookies = nameValuePairs.length, i = 0;
		Cookie[] cookies = new Cookie[numOfCookies];
		for(String pair : nameValuePairs)
		{
			String[] nameValueList = pair.split("=");
			Cookie cookie = new Cookie(nameValueList[0].trim(), nameValueList[1].trim());
			cookies[i++] = cookie;
		}
		return cookies;
	}
	HashMap<String, String> parseHttpHeaders(BufferedReader headerBuffer) throws IOException
	{
		
		HashMap<String, String> headerMap = new HashMap<String, String>();
		String headerLine;
		String headerValuePair[];
		String value;
		int i;
		boolean first;
		while(true)
		{
			first = true;
			value = "";
			headerLine = headerBuffer.readLine();
			if((headerLine.length() == 0))
			{
				break;
			}
			headerValuePair = headerLine.split(":");
			for(i = 1; i < headerValuePair.length; i++)
			{
				if(first)
				{
					value = value + headerValuePair[i];
					first = false;
				}
				else
					value = value + ":" + headerValuePair[i];
			}
			headerMap.put(headerValuePair[0].trim().toLowerCase(), value.trim());
		}
		
		return headerMap;
	}
}
