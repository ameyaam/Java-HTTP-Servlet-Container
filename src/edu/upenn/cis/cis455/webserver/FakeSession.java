package edu.upenn.cis.cis455.webserver;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.apache.log4j.Logger;

/**
 * @author Todd J. Green
 */
public class FakeSession implements HttpSession {
	private Properties m_props = new Properties();
	private boolean m_valid;
	Date creationTime;
	UUID sessionId;
	long lastAccessTime;
	int maxInactiveInterval = 2*60*60;
	boolean isNew = false;
	static Logger log = Logger.getLogger(FakeSession.class.getName());
	
	public FakeSession()
	{
		isNew = true;
		sessionId = UUID.randomUUID();
		creationTime = new Date();
		m_valid = true;
		lastAccessTime = new Date().getTime();
	}
	
	public long getCreationTime() {
		// TODO Auto-generated method stub
		if(!isValid())
		{
			throw new IllegalStateException();
		}
		return creationTime.getTime();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getId()
	 */
	public String getId() {
		// TODO Auto-generated method stub
		if(!isValid())
		{
			throw new IllegalStateException();
		}
		return sessionId.toString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		if(!isValid())
		{
			throw new IllegalStateException();
		}
		return lastAccessTime;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 */
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return HttpServer.context;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
	 */
	public void setMaxInactiveInterval(int arg0) {
		// TODO Auto-generated method stub
		if(arg0 == -1)
		{
			maxInactiveInterval = (int) Double.POSITIVE_INFINITY;
		}else{
			maxInactiveInterval = arg0;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
	 */
	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return maxInactiveInterval;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getSessionContext()
	 */
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		if(!isValid())
		{
			throw new IllegalStateException();
		}
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
	 */
	public Object getValue(String arg0) {
		// TODO Auto-generated method stub
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		if(!isValid())
		{
			throw new IllegalStateException();
		}
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValueNames()
	 */
	public String[] getValueNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		if(!isValid())
		{
			throw new IllegalStateException();
		}
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
	 */
	public void putValue(String arg0, Object arg1) {
		if(!isValid())
		{
			throw new IllegalStateException();
		}
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		if(!isValid())
		{
			throw new IllegalStateException();
		}
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
	 */
	public void removeValue(String arg0) {
		if(!isValid())
		{
			throw new IllegalStateException();
		}
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#invalidate()
	 */
	public void invalidate() {
		if(!isValid())
		{
			throw new IllegalStateException();
		}
		m_valid = false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#isNew()
	 */
	public boolean isNew() {
		// TODO Auto-generated method stub
		if(!isValid())
		{
			throw new IllegalStateException();
		}
		return isNew;
	}

	boolean isValid() {
		if((new Date().getTime()) - lastAccessTime > TimeUnit.MINUTES.toMillis(maxInactiveInterval))
		{
			m_valid = false;
			log.debug("Invalid session");
			HttpServer.sessionMap.remove(sessionId);
		}
		return m_valid;
	}

}
