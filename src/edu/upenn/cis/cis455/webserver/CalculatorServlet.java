package edu.upenn.cis.cis455.webserver;

import java.io.*;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

public class CalculatorServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
  {
		System.out.println("[DEBUG] Printing from the servlet");
		/*Cookie[] cookies = request.getCookies();
		if(cookies != null)
		{
			for(Cookie cookie: cookies)
			{
				System.out.println(cookie.getName() + " " + cookie.getValue());
			}
		}*/
		//for(String value :  request.getParameterValues("name"))
		//System.out.println(request.getParameter("some"));
		System.out.println(request.getParameter("name"));
		System.out.println(request.getParameterValues("name")[1]);
		System.out.println(request.getParameter("name2"));
		//System.out.println(request.getParameterMap());
		
		
		response.setStatus(200);
		//request.getSession(true);
		response.setHeader("content-type", "text/html");
		response.setHeader("connection", "close");
		String body = "This is some text";
		response.setContentLength(body.length());
		
		HttpSession session = request.getSession();
		session.setMaxInactiveInterval(1);
		//System.out.println("[DEBUG] Session creation time: " + session.getCreationTime());
		//System.out.println("[DEBUG] Session last access time: " + session.getLastAccessedTime());
		
		//response.addCookie(new Cookie("somname","somevalue"));
		//response.addCookie(new Cookie("moarname","moarvalue"));
		//response.sendRedirect("HW1");
		//PrintWriter writer = response.getWriter();
		//writer.write(body);
		//response.sendError(404);
		//System.out.println("[DEBUG] REQUESTED:" + request.getRequestedSessionId());
		//response.flushBuffer();
  }
}
  