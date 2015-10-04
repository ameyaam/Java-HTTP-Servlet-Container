package edu.upenn.cis.cis455.webserver;

import javax.servlet.*;

import org.apache.log4j.Logger;

import java.util.*;

public class FakeConfig implements ServletConfig {
	private String name;
	private FakeContext context;
	private HashMap<String,String> initParams;
	static Logger log = Logger.getLogger(FakeConfig.class.getName());
	
	public FakeConfig(String name, FakeContext context) {
		this.name = name;
		this.context = context;
		initParams = new HashMap<String,String>();
	}

	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public ServletContext getServletContext() {
		return context;
	}
	
	public String getServletName() {
		return name;
	}

	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
