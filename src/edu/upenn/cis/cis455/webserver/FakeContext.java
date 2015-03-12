package edu.upenn.cis.cis455.webserver;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Nick Taylor
 */
public class FakeContext implements ServletContext {
	private HashMap<String,Object> attributes;
	private HashMap<String,String> initParams;
	static Logger log = Logger.getLogger(FakeContext.class.getName());
	
	public FakeContext() {
		attributes = new HashMap<String,Object>();
		initParams = new HashMap<String,String>();
	}
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	public Enumeration getAttributeNames() {
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public ServletContext getContext(String name) {
		return HttpServer.context;
	}
	
	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public int getMajorVersion() {
		return 2;
	}
	
	public String getMimeType(String path) {

		return null;
	}
	
	public int getMinorVersion() {
		return 4;
	}
	
	public RequestDispatcher getNamedDispatcher(String name) {
		return null;
	}
	
	public String getRealPath(String path) {
		return null;
	}
	
	public RequestDispatcher getRequestDispatcher(String name) {
		return null;
	}
	
	public java.net.URL getResource(String path) {
		return null;
	}
	
	public java.io.InputStream getResourceAsStream(String path) {
		return null;
	}
	
	public String getMatchingResource(String suppliedPath, String resourcePath)
	{
		int i;
		int resourcePathLength = resourcePath.length();
		int suppliedPathLength = suppliedPath.length();
		String requiredPath = "";
		if(resourcePathLength <= suppliedPathLength)
			return null;
		for(i = 0; i < suppliedPathLength; i++)
		{
			if(suppliedPath.charAt(i) != resourcePath.charAt(i))
			{
				return null;
			}
			requiredPath += resourcePath.charAt(i);
		}
		while(i < resourcePathLength)
		{
			requiredPath += resourcePath.charAt(i);
			if(resourcePath == "/")
				break;
			i++;
		}
		return requiredPath;
	}
	
	public java.util.Set getResourcePaths(String path) {
		//getMatchingResource(path, resourcePath);
		
		return null;
	}
	
	public String getServerInfo() {
		return "Java HTTP Server";
	}
	
	public Servlet getServlet(String name) {
		return null;
	}
	
	public String getServletContextName() {
		return "Test Harness";
	}
	
	public Enumeration getServletNames() {
		Enumeration servletNamesEnum;
		Vector servletNamesVector = new Vector();
		for (Entry<String, HttpServlet> entry : HttpServer.servlets.entrySet()) {
		    String servletName = entry.getKey();
		    servletNamesVector.add(servletName);
		}
		servletNamesEnum = servletNamesVector.elements();
		return null;
	}
	
	public Enumeration getServlets() {
		return null;
	}
	
	public void log(Exception exception, String msg) {
		log(msg, (Throwable) exception);
	}
	
	public void log(String msg) {
		System.err.println(msg);
	}
	
	public void log(String message, Throwable throwable) {
		System.err.println(message);
		throwable.printStackTrace(System.err);
	}
	
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	
	public void setAttribute(String name, Object object) {
		if(object==null)
            removeAttribute(name);
        else
            attributes.put(name, object);
		
	}
	
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
