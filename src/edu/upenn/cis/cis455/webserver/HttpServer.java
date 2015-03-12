package edu.upenn.cis.cis455.webserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;
import java.text.ParseException;
import java.util.regex.Matcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.webserver.TestHarness.Handler;



interface shutdownThreads {
	void stopThreads();
}

public class HttpServer
	{
		//public static ThreadPool threadPool;
	 	public static List<WorkerThread> threadPool = new ArrayList<WorkerThread>();
		public static int port;
		public static String rootPath;
		public static HashMap<String,FakeSession> sessionMap = new HashMap<String, FakeSession>();
		static boolean shutdownFlag = false;
		public static Handler handler;
		static FakeContext context;
		static HashMap<String,HttpServlet> servlets;
		static HashMap<String,String> urlMap;
		static Logger log = Logger.getLogger(HttpServer.class.getName());
		
		public static void main(String[] args)
		{
			/*
			 * Main function only parses the first line of the request to see if request is to shutdown.
			 * If shutdown, then shuts down all threads safely
			 * Else creates a thread to handle the request and passes all the parsed data to the thread
			 * to handle the request.
			 */
			BlockingQueue queue = new BlockingQueue();
			Socket connected = null;
			BufferedReader checkForShutdown;
			DataOutputStream outToClient;
			//BasicConfigurator.configure();
			try{
				if(args.length != 3)
				{
					System.out.println("Name: Ameya More\nSEAS login: ameyam ");
					return;
				}
				
				
				String httpMethod;
				String requestPath;
				String httpVersion;
				String requestString = null;
				StringTokenizer tokenizer;
				port = Integer.parseInt(args[0]);
				rootPath = args[1];
				int maxNoThreads = 10;
				int i = 0;
				
				for(i = 0; i < maxNoThreads; i++)
				{
					WorkerThread worker = new WorkerThread(queue);
					worker.start();
					threadPool.add(worker);		
				}
				
				handler = TestHarness.parseWebdotxml(args[2]);
				context = TestHarness.createContext(handler);		
				servlets = TestHarness.createServlets(handler, context);		
				urlMap = TestHarness.createServerUrlMap(handler);
				
				ServerSocket Server = new ServerSocket (port, 10, InetAddress.getByName("127.0.0.1"));
				System.out.println("TCPServer Waiting for client on port " + port);
				log.debug("TCPServer Waiting for client on port " + port);
				while(true) {	
					connected = Server.accept();
					
					checkForShutdown = new BufferedReader(new InputStreamReader (connected.getInputStream()));
					outToClient = new DataOutputStream(connected.getOutputStream());
					try{
						requestString = checkForShutdown.readLine();
						if(requestString == null)
						{
							continue;
						}
					}catch(Exception e)
					{
						log.debug("Socket read exception");
						continue;
					}
					try{
						tokenizer = new StringTokenizer(requestString);

						httpMethod = tokenizer.nextToken();
						requestPath = tokenizer.nextToken();
						httpVersion = tokenizer.nextToken();
					}catch(Exception e)
					{
						log.debug(e);
						connected.close();
						continue;
					}
					/*If shutdown message encountered, then shutdown the server*/
					
					if(requestPath.endsWith("shutdown"))
					{
						String responseString = "<html><body><h4 align = \"center\"> Server is shutting down...</h4></body></html>";
						outToClient.writeBytes("HTTP/1.1 200 OK" + "\r\n");
						outToClient.writeBytes("Server: Java HTTPServer" + "\r\n");
						outToClient.writeBytes("Content-Type: text/html" + "\r\n");
						outToClient.writeBytes("Content-Length: " + Integer.toString(responseString.length()) + "\r\n");
						outToClient.writeBytes("Connection: close\r\n");
						outToClient.writeBytes("\r\n");
						outToClient.writeBytes(responseString);
						outToClient.close();
						connected.close();
						
						for(WorkerThread thread: threadPool)
						{
							thread.doStop();
							thread.join();
						}
						for (Entry<String, HttpServlet> entry : servlets.entrySet()) {
						    String key = entry.getKey();
						    HttpServlet servlet = entry.getValue();
						    servlet.destroy();
						}
						Server.close();
						return;
					}
					
					HttpRequestHandler handler = new HttpRequestHandler(connected, checkForShutdown, outToClient, httpMethod, httpVersion, requestPath);
					queue.enqueue(handler);
			
				}
			} catch(IOException e){
				log.debug(e);
			}catch(Exception e)
			{
				log.debug(e);

			}
		}
		
		static String getControlPanel(int port)
		{	
			/*String outputString = "<h4 align = \"center\">Name: Ameya More</h4>"
					+ "<h4 align = \"center\">Penn key: ameyam</h4>"
					//+ "<form action = \"http://localhost:"+ port +"/shutdown\"> <input type = \"submit\" value = \"Shutdown\"> </form>"
					+ "<p align = \"center\"><a href = \"http://localhost:"+ port +"/shutdown\"> <button> Shutdown </button></a></p>"
					+ "<table border = \"1\" align = \"center\">"
						+ "<tr><th>Thread</th><th>URL</th><th>Status</th></tr>";
			int threadNum = 0;
			for(WorkerThread thread : threadPool)
			{
				outputString = outputString + "<tr><td>" + Integer.toString(threadNum) + "</td><td>"
						+ thread.currentUrl +"</td><td>"+ thread.getState() + "</td></tr>";
				threadNum++;
			}
			outputString = outputString + "</table>";*/

			
			return null;
		}

		
		
		 public static class HttpRequestHandler{
			Socket connectedClient;
			String httpVersion;
			public String RequestPath;
			public String currentUrl;
			DataOutputStream outToClient;
			HttpRequest request = null;
			
			private HttpRequestHandler(Socket s, BufferedReader in, DataOutputStream out, String method, String version , String reqpath) throws Exception{
				this.connectedClient = s;
				this.httpVersion = version;
				this.RequestPath = reqpath;
				this.currentUrl = "http://localhost:"+ Integer.toString(port) +"/" + reqpath.replaceFirst("/", "");
				this.outToClient = out;
				request = new HttpRequest(in, method, version, port, s);
			}
			
			static Date convertStringToDate(String inputDate)
			{
				//System.out.println("received this: " + inputDate);
				Date receivedDateObj = new Date();
				inputDate = inputDate.replace(" GMT", "");
				String[] formats = {//"ddd, dd mmm yyyy HH:mm:ss", 
									"EEE, dd MMM yyyy HH:mm:ss",
									"EEEE, dd-MMM-yy HH:mm:ss", 
									"EEE MMM dd HH:mm:ss yyyy"};
				
				for (String parse : formats) {
	                	
	                try {
	                	SimpleDateFormat formatter = new SimpleDateFormat(parse);
	                	//System.out.println(inputDate);
	                	formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	                    receivedDateObj = formatter.parse(inputDate);
	                    //System.out.println("PARSE SUCCESSFUL");
	                    return receivedDateObj;
	                    //System.out.println("Printing the value of " + parse);
	                } catch (ParseException e) {
	                	log.debug(e);
	                }
	                catch(Exception e)
	                {
	                	log.debug(e);
	                }
	            }
				return receivedDateObj;
			}
			
			static String getServerTime() {
		        Calendar calendar = Calendar.getInstance();
		        SimpleDateFormat dateFormat = new SimpleDateFormat(
		                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		        return dateFormat.format(calendar.getTime());
		    }
			static boolean processIfUnmodifiedSince(String receivedDateString, File requiredObject, DataOutputStream outToClient) throws IOException
			{
				Date lastModified = new Date(requiredObject.lastModified());
				Date receivedDate = convertStringToDate(receivedDateString);
		
				if(receivedDate != null)
				{
					//System.out.println("Parsed this date: " + receivedDate);
					if(lastModified.after(receivedDate))
					{
						String statusLine = "HTTP/1.1 412 Precondition failed" + "\r\n";
						String contentTypeLine = "Content-Type: text/html" + "\r\n";
						
						outToClient.writeBytes(statusLine);
						//outToClient.writeBytes("Host: localhost:" + port + "\r\n");
						outToClient.writeBytes("Server: Java HTTP server" + "\r\n");
						outToClient.writeBytes(contentTypeLine);
						//outToClient.writeBytes("Date: " + date + "\r\n");
						outToClient.writeBytes("Connection: close\r\n");
						outToClient.writeBytes("\r\n");
						outToClient.close();
						return true;
					}
				}
				return false;
			}
				
				
			public static String removeLast(String str) {
				return str.substring(0,str.length()-1);
			}
			
			String getdate(long lastmodified){
		        Date d = new Date(lastmodified);
		        final SimpleDateFormat sdf =
		                new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		        String date = (sdf.format(d)) + " GMT";
		        return date;
		    }
			
			boolean matchSucess(String storedUrl, String requestUrl)
			{
				//System.out.println("Calling from matchSuccess");
				//System.out.println(storedUrl + " " + requestUrl);
				requestUrl = requestUrl + "/";
				boolean endsWithStar = false;
				if(storedUrl.endsWith("*"))
				{
					storedUrl = removeLast(storedUrl);
					endsWithStar = true;
				}
				else if(!storedUrl.endsWith("/"))
				{
					storedUrl = storedUrl + "/";
				}
				if(endsWithStar == false)
				{
					if(!requestUrl.equals(storedUrl))
						return false;
				}
				if(requestUrl.startsWith(storedUrl))
				{
					return true;
				}
				return false;
			}
			public void run()
			{
				try {
					String date = getServerTime();
					String htmlStart = "<html><style>table {border-collapse: collapse;} table, th, td { border: 1px solid black;}</style> " +
							"<title>HTTP Multithreaded server </title>" +
							"<body><h1 align = \"center\">CIS 455/555 Assignment 1</h1><br>";
					String htmlEnd = "</body>" +
							"</html>";
					String responseString = null;
					String statusLine = null;
					String contentTypeLine = null;
					String serverDetails = "Server: Java HTTPServer" + "\r\n";
					String contentLength = null;
					String servletName = null;
					
					if(!httpVersion.startsWith("HTTP/"))
					{
						responseString = htmlStart + "<h4> Bad request: Unknown protocol </h4>" + htmlEnd;
						contentLength = "Content-Length: " + responseString.length() + "\r\n";
						statusLine = "HTTP/1.1 400 Bad Request" + "\r\n";
						contentTypeLine = "Content-Type: text/html" + "\r\n";
						
						outToClient.writeBytes(statusLine);
						//outToClient.writeBytes("Host: localhost:" + port + "\r\n");
						outToClient.writeBytes(serverDetails);
						outToClient.writeBytes(contentTypeLine);
						outToClient.writeBytes(contentLength);
						outToClient.writeBytes("Date: " + date + "\r\n");
						outToClient.writeBytes("Connection: close\r\n");
						outToClient.writeBytes("\r\n");
						outToClient.writeBytes(responseString);
						outToClient.close();
						connectedClient.close();
						return;
					}

					if(httpVersion.equals("HTTP/1.1"))
					{
						if(request.headerValueMap.get("host") == null)
						{
							//System.out.println("No host found");
							responseString = htmlStart + "<h4> Bad request: No host header found </h4>" + htmlEnd;
							contentLength = "Content-Length: " + responseString.length() + "\r\n";
							statusLine = "HTTP/1.1 400 Bad Request" + "\r\n";
							contentTypeLine = "Content-Type: text/html" + "\r\n";
							
							outToClient.writeBytes(statusLine);
							//outToClient.writeBytes("Host: localhost:" + port + "\r\n");
							outToClient.writeBytes(serverDetails);
							outToClient.writeBytes(contentTypeLine);
							outToClient.writeBytes(contentLength);
							outToClient.writeBytes("Date: " + date + "\r\n");
							outToClient.writeBytes("Connection: close\r\n");
							outToClient.writeBytes("\r\n");
							outToClient.writeBytes(responseString);
							outToClient.close();
							connectedClient.close();
							return;
						}
					}
					
					/* If control, then create control panel and send reponse*/
					if(RequestPath.endsWith("/control"))
					{
						/*getConrtolPanel creates the HTML for the control panel page*/
						//responseString = htmlStart + getControlPanel(port) + htmlEnd;
						responseString = getControlPanel(port);
						//contentLength = "Content-Length: " + responseString.length() + "\r\n";
						statusLine = "HTTP/1.1 200 OK \r\n";
						contentTypeLine = "Content-Type: text/html" + "\r\n";
						FileInputStream fileStream = new FileInputStream("/home/cis455/workspace/HW1/www/htmlLayout.html");
						contentLength = "Content-Length: " + Integer.toString(fileStream.available()) + "\r\n";
						
						outToClient.writeBytes(statusLine);
						//outToClient.writeBytes("Host: localhost:" + port + "\r\n");
						outToClient.writeBytes(serverDetails);
						outToClient.writeBytes(contentTypeLine);
						outToClient.writeBytes(contentLength);
						outToClient.writeBytes("Date: " + date + "\r\n");
						contentLength = "Content-Length: " + Integer.toString(fileStream.available()) + "\r\n";
						outToClient.writeBytes("Connection: close\r\n");
						outToClient.writeBytes("\r\n");
		
						byte[] buffer = new byte[1024] ;
						int bytesRead;
						
						while ((bytesRead = fileStream.read(buffer)) != -1 )
						{
							outToClient.write(buffer, 0, bytesRead);
						}
						fileStream.close();
						//outToClient.writeBytes(responseString);
						outToClient.close();
						connectedClient.close();
						return;
					}
					
					if(RequestPath.startsWith("http://"))
					{
						int i, count = 3;
						for(i = 0; i < RequestPath.length(); i++)
						{
							if(RequestPath.charAt(i) == '/')
							{
								count--;
							}
							if(count == 0)
								break;
						}
						//i++;
						if(count != 0)
						{
							RequestPath = "/";
						}
						else
							RequestPath = RequestPath.substring(i);
					}
				
					/*Implement GET Method*/
					String relative_path = RequestPath;
					relative_path = URLDecoder.decode(relative_path, "UTF-8");
					
					if(request.httpMethod.equals("GET") || request.httpMethod.equals("POST"))
					{	
						if(relative_path.contains("?"))
						{
							String temp = "";
							int index = 0;
							while(true)
							{
								
								if(relative_path.charAt(index) == '?')
									break;
								temp = temp + relative_path.charAt(index);
								index++;
								
							}
							request.queryString = relative_path.substring(index + 1);
							relative_path = temp;
						}else
						{
							request.queryString = null;
						}
						
					}
					relative_path = relative_path.replace("//", "/");
					if(relative_path.endsWith("/"))
					{
						relative_path = removeLast(relative_path);
					}
					
					/*MAKE SURE REQUEST PATH NEVER ENDS WITH A / HERE!!!!!!*/
					request.relativePath = relative_path;

					for (Entry<String, String> entry : urlMap.entrySet()) {
					    String key = entry.getKey();
					    String value = entry.getValue();
					    if(matchSucess(value, relative_path))
				        {
				        	servletName = key;
				        	request.servletUrl = value;
				        	break;
				        }
					}
					
					if(servletName != null)
					{	
						HttpServlet servlet = servlets.get(servletName);
						String sessionId = null;
						if(request.cookie != null){
							for(Cookie cookie : request.cookie)
							{
								
								if(cookie.getName().equals("JSESSIONID"))
								{
									sessionId = cookie.getValue();
								}
							}
						}
						FakeSession httpSession = null;
						if(sessionId != null)
						{
							httpSession = sessionMap.get(sessionId);
						}
						/*session will be created when get session is called by the servlet on the
						 * request object if it is null here*/
						FakeResponse servletResponse = new FakeResponse(outToClient, request);
						FakeRequest servletRequest = new FakeRequest(httpSession, request, servletResponse);					
						/* Update last access time of the session*/
						
						
						/*set GET parameters here*/
						if(request.queryString != null)
						{
							String[] parameters = request.queryString.split("&");
							for (String parameter: parameters)
							{
								String[] nameValue = parameter.split("=");
								servletRequest.setParameter(nameValue[0], nameValue[1]);
							}
							
						}
						
						servlet.service(servletRequest, servletResponse);
						if(httpSession != null)
						{
							httpSession.lastAccessTime = new Date().getTime();
						}
						servletResponse.flushBuffer();
						outToClient.close();
						connectedClient.close();
						return;
					}
					String path = rootPath + relative_path;
					File requiredObject = new File(path);
					if(request.httpMethod.equals("GET") || request.httpMethod.equals("HEAD"))
					{		
						/*TODO: Ensure that rootPath is parent of path*/
						if(!requiredObject.exists())
						{
							/*If the required file is not found, send 404*/
							responseString = htmlStart + "<h1>Error: Not Found</h1>" + htmlEnd;
							contentLength = "Content-Length: " + responseString.length() + "\r\n";
							statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
							contentTypeLine = "Content-Type: text/html" + "\r\n";
							
							if(request.headerValueMap.get("expect") != null)
							{
								if(request.headerValueMap.get("expect").equals("100-continue"))
								{
									outToClient.writeBytes("HTTP/1.1 100 Continue\r\n");
									outToClient.writeBytes("\r\n");
								}
							}
							outToClient.writeBytes(statusLine);
							//outToClient.writeBytes("Host: localhost:" + port + "\r\n");
							outToClient.writeBytes(serverDetails);
							outToClient.writeBytes(contentTypeLine);
							outToClient.writeBytes(contentLength);
							outToClient.writeBytes("Date: " + date + "\r\n");
							outToClient.writeBytes("Connection: close\r\n");
							outToClient.writeBytes("\r\n");
							if(request.httpMethod.equals("GET"))
								outToClient.writeBytes(responseString);
							outToClient.close();
							connectedClient.close();
							return;	
						}
						/* Ensure required directory is not outside the allowed root.
						 * If not allowed to access then send 403
						 */
						if(!parentCheck(new File(path), new File(rootPath)))
						{
							responseString = htmlStart + "Forbidden" + htmlEnd;
							contentLength = "Content-Length: " + responseString.length() + "\r\n";
							statusLine = "HTTP/1.1 403 Forbidden" + "\r\n";
							contentTypeLine = "Content-Type: text/html" + "\r\n";
							if(request.headerValueMap.get("expect") != null)
							{
								if(request.headerValueMap.get("expect").equals("100-continue"))
								{
									outToClient.writeBytes("HTTP/1.1 100 Continue\r\n");
									outToClient.writeBytes("\r\n");
								}
							}
							outToClient.writeBytes(statusLine);
							//outToClient.writeBytes("Host: localhost:" + port + "\r\n");
							outToClient.writeBytes(serverDetails);
							outToClient.writeBytes(contentTypeLine);
							outToClient.writeBytes(contentLength);
							outToClient.writeBytes("Date: " + date + "\r\n");
							outToClient.writeBytes("Connection: close\r\n");
							outToClient.writeBytes("\r\n");
							if(request.httpMethod.equals("GET"))
								outToClient.writeBytes(responseString);
							outToClient.close();
							connectedClient.close();
							return;
						}						
						/* if the requested path in the URL is a directory then create HTML to
						 * list the directories and send response
						 */
						if(requiredObject.isDirectory())
						{
							//System.out.println("Detected_directory");
							String listOfFiles = "<table><tr><th>File</th><th>Last modified</th><th>Size</th></tr><tr><td><a href = \"http://localhost:"
											+ Integer.toString(port) + relative_path  + "/..\">../ </a></td><td></td><td></td></tr>";
							File [] files;
							files = requiredObject.listFiles();
							for(File file :files)
							{
								if (file.isDirectory())
								{
									listOfFiles = listOfFiles + "<tr><td><a href = \"http://localhost:"
											+ Integer.toString(port) + relative_path  + "/" + file.getName() 
											+ "\">" + file.getName() + "/</a></td><td>" + getdate(file.lastModified()) + "</td><td>" + file.length() + "</td></tr>";
								}
								else
								{
									listOfFiles = listOfFiles + "<tr><td><a href = \"http://localhost:"
											+ Integer.toString(port) + relative_path +"/" + file.getName() 
											+ "\">" + file.getName() + "</a></td><td>" + getdate(file.lastModified()) + "</td><td>" + file.length() + "</td></tr>";
						
								}
							}
							listOfFiles = listOfFiles + "</table>";
							//System.out.println(listOfFiles);
							responseString = htmlStart + listOfFiles + htmlEnd;
							//System.out.println(responseString);
							contentLength = "Content-Length: " + responseString.length() + "\r\n";
							statusLine = "HTTP/1.1 200 OK" + "\r\n";
							contentTypeLine = "Content-Type: text/html" + "\r\n";
							
							if(request.headerValueMap.get("expect") != null)
							{
								if(request.headerValueMap.get("expect").equals("100-continue") )
								{
									//System.out.println("Found expect");
									outToClient.writeBytes("HTTP/1.1 100 Continue\r\n");
									outToClient.writeBytes("\r\n");
								}
							}
							outToClient.writeBytes(statusLine);
							//outToClient.writeBytes("Host: localhost:" + port + "\r\n");
							outToClient.writeBytes(serverDetails);
							outToClient.writeBytes(contentTypeLine);
							outToClient.writeBytes(contentLength);
							outToClient.writeBytes("Date: " + date + "\r\n");
							outToClient.writeBytes("Connection: close\r\n");
							outToClient.writeBytes("\r\n");
							if(request.httpMethod.equals("GET"))
								outToClient.writeBytes(responseString);
							outToClient.close();
							connectedClient.close();
						}
						else
						{
							/*Code for handling files*/
							/*Check is resource has been modified*/
							if(request.headerValueMap.get("if-modified-since") != null)
							{
								
								//convertStringToDate();
								Date lastModified = new Date(requiredObject.lastModified());
								Date receivedDate = convertStringToDate(request.headerValueMap.get("if-modified-since"));
						
								if(receivedDate != null)
								{
									//System.out.println("Parsed this date: " + receivedDate);
									if(lastModified.before(receivedDate))
									{
										responseString = htmlStart + "not modified" + htmlEnd;
										//System.out.println(responseString);
										contentLength = "Content-Length: " + responseString.length() + "\r\n";
										statusLine = "HTTP/1.1 304 Not Modified" + "\r\n";
										contentTypeLine = "Content-Type: text/html" + "\r\n";
										if(request.headerValueMap.get("expect") != null)
										{
											if(request.headerValueMap.get("expect").equals("100-continue"))
											{
												//System.out.println("Found expect");
												outToClient.writeBytes("HTTP/1.1 100 Continue\r\n");
												outToClient.writeBytes("\r\n");
											}
										}
										outToClient.writeBytes(statusLine);
										//outToClient.writeBytes("Host: localhost:" + port + "\r\n");
										outToClient.writeBytes(serverDetails);
										outToClient.writeBytes(contentTypeLine);
										outToClient.writeBytes(contentLength);
										outToClient.writeBytes("Date: " + date + "\r\n");
										outToClient.writeBytes("Connection: close\r\n");
										outToClient.writeBytes("\r\n");
										outToClient.close();
										connectedClient.close();
										return;
									}
								}
							}
							
							if(request.headerValueMap.get("if-unmodified-since") != null)
							{
								if(processIfUnmodifiedSince(request.headerValueMap.get("if-unmodified-since"), requiredObject, outToClient))
									return; /*this function has sent the response. Simply return*/
							}
							//System.out.println("FOUND A FILE!");
							FileInputStream fileStream = new FileInputStream(path);
							contentLength = "Content-Length: " + Integer.toString(fileStream.available()) + "\r\n";
							if (path.endsWith(".html") || path.endsWith(".html"))
							{
								contentTypeLine = "Content-Type: text/html" + "\r\n";
							}
							else if(path.endsWith(".pdf"))
							{
								contentTypeLine = "Content-Type: application/pdf" + "\r\n";
							}
							else if(path.endsWith(".txt"))
							{
								contentTypeLine = "Content-Type: text/plain" + "\r\n";
							}
							else if(path.endsWith(".jpg") || path.endsWith(".jpeg"))
							{
								contentTypeLine = "Content-Type: image/jpeg" + "\r\n";
							}
							else if(path.endsWith(".png"))
							{
								contentTypeLine = "Content-Type: image/png" + "\r\n";
							}
							else if(path.endsWith(".gif"))
							{
								contentTypeLine = "Content-Type: image/gif" + "\r\n";
							}
							else
							{
								contentTypeLine = "Content-Type: application/octet-stream" + "\r\n";
							}
							
							statusLine = "HTTP/1.1 200 OK" + "\r\n";
							if(request.headerValueMap.get("expect") != null)
							{
								if(request.headerValueMap.get("expect").equals("100-continue"))
								{
									outToClient.writeBytes("HTTP/1.1 100 Continue\r\n");
									outToClient.writeBytes("\r\n");
								}
							}
							outToClient.writeBytes(statusLine);
							//outToClient.writeBytes("Host: localhost:" + port + "\r\n");
							outToClient.writeBytes(serverDetails);
							outToClient.writeBytes(contentTypeLine);
							outToClient.writeBytes(contentLength);
							outToClient.writeBytes("Date: " + date + "\r\n");
							outToClient.writeBytes("Connection: close\r\n");
							outToClient.writeBytes("\r\n");
							if(request.httpMethod.equals("GET"))
							{
								byte[] buffer = new byte[1024] ;
								int bytesRead;
								
								while ((bytesRead = fileStream.read(buffer)) != -1 )
								{
									outToClient.write(buffer, 0, bytesRead);
								}
								fileStream.close();
							}
							outToClient.close();
							connectedClient.close();
						}
					}
					else
					{
						/* If the request contains any other method then send 405*/
						responseString = htmlStart + "Unsupported method: " + request.httpMethod + htmlEnd;
						contentLength = "Content-Length: " + responseString.length() + "\r\n";
						statusLine = "HTTP/1.1 405 Method Not Allowed" + "\r\n";
						contentTypeLine = "Content-Type: text/html" + "\r\n";
						
						outToClient.writeBytes(statusLine);
						//outToClient.writeBytes("Host: localhost:" + port + "\r\n");
						outToClient.writeBytes(serverDetails);
						outToClient.writeBytes(contentTypeLine);
						outToClient.writeBytes(contentLength);
						outToClient.writeBytes("Date: " + date + "\r\n");
						outToClient.writeBytes("Connection: close\r\n");
						outToClient.writeBytes("\r\n");
						outToClient.writeBytes(responseString);
						outToClient.close();
						connectedClient.close();
						return;

					}

				}
				catch(Exception e)
				{
					try {
						e.printStackTrace();
						outToClient.close();
						connectedClient.close();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						log.debug(e);
					}
				}
			}
			 boolean parentCheck(File maybeChild, File possibleParent) throws IOException
			 {
			     final File parent = possibleParent.getCanonicalFile();
			     if (!parent.exists() || !parent.isDirectory()) {
			         // this cannot possibly be the parent
			         return false;
			     }

			     File child = maybeChild.getCanonicalFile();
			     while (child != null) {
			         if (child.equals(parent)) {
			             return true;
			         }
			         child = child.getParentFile();
			     }
			     // No match found, and we've hit the root directory
			     return false;
			 }			
		}
}