package edu.upenn.cis.cis455.webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author Ameya
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FakeResponse implements HttpServletResponse {

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	ArrayList<Cookie> cookies = new ArrayList<Cookie>();
	public HashMap<String,String> headers = new HashMap<String,String>();
	DataOutputStream outToClient;
	boolean committedFlag = false;
	public int statusCode = -1;
	int bufferSize = 4096;
	FakeBuffer buffer = null;
	HashMap<Integer, String> statusMsg = new HashMap<Integer, String>();
	HttpRequest httpRequest = null;
	static Logger log = Logger.getLogger(FakeResponse.class.getName());
	
	
	public FakeResponse(DataOutputStream str, HttpRequest r)
	{

		this.outToClient = str;
		statusMsg.put(SC_NOT_FOUND, "Not Found");
		statusMsg.put(SC_OK, "OK");
		statusMsg.put(SC_TEMPORARY_REDIRECT, "Redirect");
		statusMsg.put(SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
		statusMsg.put(SC_FORBIDDEN, "Forbidden");
		buffer = new FakeBuffer();
		httpRequest = r;
	}
	
	String formatHTTPDate(long date) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			return dateFormat.format(date);
		}
	
	
	public void addCookie(Cookie arg0) {
		cookies.add(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String arg0) {
		// TODO Auto-generated method stub
		System.out.println("From fakeresponse: " + arg0.toLowerCase());
		System.out.println("From fakeresponse" + headers.get("accept"));
		if(headers.get(arg0.toLowerCase()) == null)
		{
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int sc, String msg) throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted() == true)
		{
			throw new IllegalStateException();
		}
		String body = "<HTML>"
				+ "<HEAD>"
				+ "<TITLE>"
				+ "Java HTTP server"
				+ "</TITLE>"
				+ "</HEAD>"
				+ "<BODY>"
				+ "<H2>" + Integer.toString(sc) + " " + msg + "</H2>"
				+ "</BODY>"
				+ "</HTML>";
				
		statusCode = sc;
		statusMsg.put(sc, msg);
		headers.put("content-type", "text/html");
		headers.put("content-length", Integer.toString(body.length()));
		buffer.clearContent();
		buffer.write(body);
		flushBuffer();
		return;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int sc) throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted() == true)
		{
			throw new IllegalStateException();
		}
		String body = "<HTML>"
				+ "<HEAD>"
				+ "<TITLE>"
				+ "Java HTTP server"
				+ "</TITLE>"
				+ "</HEAD>"
				+ "<BODY>"
				+ "<H2>" + Integer.toString(sc) + ": Error</H2>"
				+ "</BODY>"
				+ "</HTML>";
				
		statusCode = sc;
		headers.put("content-type", "text/html");
		headers.put("content-length", Integer.toString(body.length()));
		buffer.clearContent();
		buffer.write(body);
		flushBuffer();
		return;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String arg0) throws IOException {
		String redirectUrl = null;
		if(arg0.startsWith("http://"))
		{
			/*Absolute URL*/
			redirectUrl = arg0;
		}
		else if(arg0.startsWith("/"))
		{
			redirectUrl = "http://localhost:" + HttpServer.port + arg0;
		}
		else
		{
			redirectUrl = "http://localhost:" + HttpServer.port + httpRequest.relativePath + "/" + arg0; 
		}
		statusCode = 302;
		headers.put("location", redirectUrl);
		flushBuffer();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		//System.out.println("[DEBUG] in setDateHeader");
		Date d = new Date(arg1);
        final SimpleDateFormat sdf =
                new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        headers.put(arg0.toLowerCase(), sdf.format(d));
        return;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		setDateHeader(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		headers.put(arg0.toLowerCase(), arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		if(headers.get(arg0.toLowerCase()) == null)
		{
			setHeader(arg0, arg1);
		}
		else
		{
			String value = headers.get(arg0.toLowerCase());
			value = value + "; " + arg1;
			headers.put(arg0.toLowerCase(), value);
		}
		return;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
			headers.put(arg0.toLowerCase(), Integer.toString(arg1));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
		if(headers.get(arg0.toLowerCase()) == null)
		{
			setIntHeader(arg0, arg1);
		}
		else
		{
			String value = headers.get(arg0.toLowerCase());
			value = value + "; " + Integer.toString(arg1);
			headers.put(arg0.toLowerCase(), value);
		}
		return;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int arg0) {
		// TODO Auto-generated method stub
		statusCode = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	/* NOT REQUIRED DEPRECATED*/
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return "ISO-8859-1";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		return headers.get("content-type");
	}

	/*NOT REQUIRED*/
	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		return buffer;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
		headers.put("content-length", Integer.toString(arg0));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		headers.put("content-type", arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub
		if(buffer.isWritten == true)
			throw new IllegalStateException();
		bufferSize = arg0;
		buffer = new FakeBuffer();
	}

	/* (non-Javadoc)
	 */
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return bufferSize;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
			buffer.flush();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		// TODO Auto-generated method stub
		if(isCommitted())
		{
			throw new IllegalStateException();
		}
		else
		{
			buffer.clearContent();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return committedFlag;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		// TODO Auto-generated method stub
		if(isCommitted())
		{
			throw new IllegalStateException();
		}
		else
		{
			headers = new HashMap<String, String>();
			statusCode = -1;
			buffer.clearContent();
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	public class FakeBuffer extends PrintWriter
	{
		private StringBuffer stringBuffer;
		boolean isWritten = false;
		public FakeBuffer() 
		{
			super(outToClient);
			stringBuffer = new StringBuffer(bufferSize);
		}
		
		void setBufferSize(int size)
		{
			StringBuffer newBuffer = new StringBuffer(size);
			
			
		}
		@Override
		public void write(String value) 
		{
			isWritten = true;
			if(stringBuffer.length() + value.length() >= stringBuffer.capacity())
			{
				flush();
			}
		    stringBuffer.append(value);
		    
		}
		
		@Override
		public void write(char[] charBuff)
		{
			isWritten = true;
			write(String.valueOf(charBuff));
		}
		
		@Override
		public void write(char[] data, int offset, int len)
		{
			isWritten = true;
			write(data.toString().substring(offset, offset + len));
		}
		
		@Override
		public void write(String s, int offset, int len) 
		{
			write(s.toString().substring(offset, offset + len));
		}
		
		@Override
		public void write(int value)
		{
			isWritten = true;
			write(Integer.toString(value));
		}
		
		public void flush()
		{
			try
			{
				if(!isCommitted())
				{
					//System.out.println("[DEBUG] Committing");
					outToClient.writeBytes("HTTP/1.1 " + Integer.toString(statusCode) + " " + statusMsg.get(statusCode) + "\r\n");
					for (Entry<String, String> entry : headers.entrySet()) {
					    String header = entry.getKey();
					    String value = entry.getValue();
					    outToClient.writeBytes(header + ":" + value + "\r\n");
					}
					/*Write cookies here*/
					if(cookies != null)
					{
						for(Cookie cookie : cookies)
						{
							String cookieString = cookie.getName() + "=" + cookie.getValue();
							if(cookie.getMaxAge() != -1){
								cookieString = cookieString + "; Expires=" + formatHTTPDate(cookie.getMaxAge());
							}
							if(cookie.getDomain() != null)
							{
								cookieString = cookieString + "; Domain=" + cookie.getDomain(); 
							}
							if(cookie.getPath() != null)
							{
								cookieString = cookieString + "; Path=" + cookie.getPath();
							}
							outToClient.writeBytes("set-cookie:" + cookieString + "\r\n");
						}
					}
					outToClient.writeBytes("\r\n");
					outToClient.flush();
					committedFlag = true;
				}
				outToClient.writeBytes(stringBuffer.toString());
				clearContent();
			} catch (IOException e)
			{
				log.debug(e);
			}
		}
		public void clearContent()
		{
			stringBuffer = new StringBuffer(bufferSize);
		}
	}
}
