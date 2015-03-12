package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * @author Todd J. Green
 */
public class FakeRequest implements HttpServletRequest {
	private boolean readBody = false;
	private Properties m_params = new Properties();
	private Properties m_props = new Properties();
	private FakeSession m_session = null;
	private String m_method;
	private Locale locale = null;
	private String encoding = "ISO-8859-1";
	HttpRequest httpRequest;
	FakeResponse httpServletResponse;
	static Logger log = Logger.getLogger(FakeRequest.class.getName());
	
	public FakeRequest() {
	}
	
	public FakeRequest(FakeSession session, HttpRequest r, FakeResponse sr) {
		m_session = session;
		httpRequest = r;
		httpServletResponse = sr;
		setMethod(httpRequest.httpMethod);
	}
	
	/*TODO: See if this returns null in any case*/
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		return BASIC_AUTH;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		return httpRequest.cookie;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String arg0) {
		Date date;
		String dateString;
		dateString = httpRequest.headerValueMap.get(arg0.toLowerCase());
		if(dateString == null)
			return -1;
		date = HttpServer.HttpRequestHandler.convertStringToDate(dateString);
		return date.getTime();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String arg0) {
		return httpRequest.headerValueMap.get(arg0.toLowerCase());
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	
	public Enumeration getHeaders(String arg0) {
		Enumeration headerValuesEnum;
		String headerValueString = httpRequest.headerValueMap.get(arg0.toLowerCase());
		String[] values = headerValueString.split(",");
		
		Vector headerValueVector = new Vector();
		for (String value:values)
		{
			headerValueVector.add(value);
		}
		headerValuesEnum = headerValueVector.elements();
		return headerValuesEnum;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration getHeaderNames() {
		Enumeration headerEnum;
		Vector headerVector = new Vector();
		for (Entry<String, String> entry : httpRequest.headerValueMap.entrySet())
		{
		    headerVector.add(entry.getKey());
		}
		headerEnum = headerVector.elements();
		return headerEnum;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader (String arg0) throws NumberFormatException {
		int valueInt;
		String valueStr = httpRequest.headerValueMap.get(arg0.toLowerCase());
		if(valueStr == null)
			return -1;
		valueInt = Integer.parseInt(valueStr);
		return valueInt;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return m_method;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	/*TODO: Verify this implementation*/
	public String getPathInfo() {
		if(!httpRequest.servletUrl.endsWith("*"))
			return null;
		int index;
		String requestUrl = httpRequest.relativePath + "/";
		String servletUrl = httpRequest.servletUrl;
		servletUrl = HttpServer.HttpRequestHandler.removeLast(servletUrl);
		index = servletUrl.length();
		return "/" + requestUrl.substring(index, requestUrl.length());
	}

	/*NOT REQUIRED*/
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	/*TODO check if correct to return "" always*/
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		// TODO Auto-generated method stub
		return "";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		return httpRequest.queryString;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	/*NOT REQUIRED*/
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/*NOT REQUIRED*/
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		String sessionId = null;
		if(httpRequest.cookie != null)
		{	
			for(Cookie cookie : httpRequest.cookie)
			{
				if (cookie.getName().equals("JSESSIONID"))
				{
					sessionId = cookie.getValue();
				}
			}
		}
		return sessionId;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		// TODO Auto-generated method stub
		return httpRequest.relativePath;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		StringBuffer url = new StringBuffer("http://" + httpRequest.headerValueMap.get("host") + httpRequest.relativePath);
		return url;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		// TODO Auto-generated method stub
		String servletUrl = httpRequest.servletUrl;
		if(servletUrl.endsWith("*"))
		{
			servletUrl = HttpServer.HttpRequestHandler.removeLast(servletUrl);
			servletUrl = HttpServer.HttpRequestHandler.removeLast(servletUrl);
		}
		return servletUrl;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public FakeSession getSession(boolean arg0) {
		if (arg0) {
			if (! hasSession()) {
				//System.out.println("[DEBUG] Creating a new session");
				m_session = new FakeSession();
				Cookie cookie = new Cookie("JSESSIONID", m_session.getId());
				//cookie.setMaxAge(expiry);
				HttpServer.sessionMap.put(m_session.getId(), m_session);
				httpServletResponse.addCookie(cookie);
			}else
			{
				m_session.isNew = false;
			}
		} else {
			if (! hasSession()) {
				m_session = null;
			}else
			{
				m_session.isNew = false;
			}
		}
		return m_session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public FakeSession getSession() {
		return getSession(true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return m_session.isValid();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	/*NOT REQUIRED: DEPRECATED*/
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		/*String contentType = httpRequest.headerValueMap.get("content-type");
		int endOffset;
		try{
			Pattern pattern = Pattern.compile("\\bcharset\\b");
			Matcher matcher = pattern.matcher(contentType);
		    matcher.find();
		    endOffset = matcher.end();
		}catch(Exception e)
		{
			return null;
		}
		String encoding = "";
	   // System.out.println("endoffset: " + endOffset);
	    while(endOffset < contentType.length())
	    {
	    	if(contentType.charAt(endOffset) == '=')
	    	{
	    		endOffset++;
	    		continue;
	    	}
	    	if(contentType.charAt(endOffset) == ';' || contentType.charAt(endOffset) == ',')
	    		break;
	    	encoding = encoding + contentType.charAt(endOffset);
	    	endOffset++;
	    }
	    System.out.println("encoding: " + encoding);*/
		return encoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	/*TODO: FIND OUT HOW TO IMPLEMENT THIS FUNCITON*/
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		encoding = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		String contentLengthStr;
		contentLengthStr = httpRequest.headerValueMap.get("content-length");
		if(contentLengthStr == null)
			return -1;
		return Integer.parseInt(contentLengthStr);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		return httpRequest.headerValueMap.get("content-type");
	}

	/*NOT REQUIRED*/
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String arg0) {
		String value;
		String[] temp;
		if(httpRequest.httpMethod.equals("GET"))
		{
			value = m_params.getProperty(arg0);
			if(value != null)
				return value.split("#")[0];
			return null;
		}
		else if(httpRequest.httpMethod.equals("POST"))
		{
			if(!readBody)
			{
				readBody = true;
				StringBuffer sb = new StringBuffer();
				int ch;
				int contentLength = Integer.parseInt(httpRequest.headerValueMap.get("content-length"));
				//System.out.println("[DEBUG] Content length:" + contentLength);
				try {
					while(true)
					{
						    ch = httpRequest.inFromClient.read();
						    if(ch == -1)
						    	break;
							if(contentLength == 1){
								sb.append((char)ch);
								break;
							}
							sb.append((char)ch);
							contentLength--;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.debug(e);
					e.printStackTrace();
				}
				
				String lines[];
				String parameterStr = sb.toString();
				if(parameterStr.contains("\n"))
				{
					lines = parameterStr.split("\n");
					for(String line:lines)
					{
						String[] paramValue = line.split("=");
						setParameter(paramValue[0], paramValue[1]);
					}
				}else
				{
					String[] parameters = parameterStr.split("&");
					
					for (String parameter: parameters)
					{
						String[] paramValue = parameter.split("=");
						setParameter(paramValue[0],paramValue[1]);
					}
				}
				
			}
		}
		value = m_params.getProperty(arg0);
		if(value != null)
			return value.split("#")[0];
		return null;
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration getParameterNames() {
		/* getParameter will read the POST body in case of a POST request
		 * so m_params will be initialized*/
		getParameter("xxxx");
		return m_params.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String arg0) {
		// TODO Auto-generated method stub
		getParameter("xxxx");
		String valueStr = m_params.getProperty(arg0);
		if(valueStr != null){
			String[] values = valueStr.split("#");
			return values;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map getParameterMap() {
		// TODO Auto-generated method stub
		getParameter("xxxx");
		Map<String, String[]> parameterValueMap = new HashMap();
		for (Entry<Object, Object> entry : m_params.entrySet()) {
		    String key = (String) entry.getKey();
		    String value = (String) entry.getValue();
		    
		    String[] values = value.split("#");
		    parameterValueMap.put(key, values);
		}
		//System.out.println("in getParameterMap: " + values[1]);
		return parameterValueMap;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		// TODO Auto-generated method stub
		return httpRequest.httpVersion;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		// TODO Auto-generated method stub
		return "http";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		// TODO Auto-generated method stub
		return httpRequest.headerValueMap.get("host");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		// TODO Auto-generated method stub
		return httpRequest.port;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	/* Set proper encoding*/
	public BufferedReader getReader() throws
	IOException {
		return httpRequest.inFromClient;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return httpRequest.socket.getInetAddress().toString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return httpRequest.socket.getInetAddress().getCanonicalHostName();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	
	public void setLocale(Locale l)
	{
		locale = l;
		return;
	}
	
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return locale;
	}

	/*NOT REQUIRED*/
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	/*NOT REEQUIRED*/
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	/*NOT REQURED: DEPRECATED*/
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return httpRequest.socket.getPort();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		// TODO Auto-generated method stub
		return httpRequest.headerValueMap.get("host");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return httpRequest.socket.getLocalAddress().toString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return httpRequest.socket.getLocalPort();
	}

	void setMethod(String method) {
		m_method = method;
	}
	
	void setParameter(String key, String value) {
		if(m_params.getProperty(key) == null){
			m_params.setProperty(key, value);
		}
		else
		{
			String temp = m_params.getProperty(key);
			temp = temp + "#" + value;
			m_params.setProperty(key, temp);
		}
	}
	
	void clearParameters() {
		m_params.clear();
	}
	
	boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}

}
