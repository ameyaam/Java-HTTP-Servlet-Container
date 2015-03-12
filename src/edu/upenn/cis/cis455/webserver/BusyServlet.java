package edu.upenn.cis.cis455.webserver;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class BusyServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("In busy servlet 1");
		response.setContentType("text/html");
		System.out.println("In busy servlet 2");
		PrintWriter out = response.getWriter();
		out.write("<HTML><HEAD><TITLE>Busy Servlet</TITLE></HEAD><BODY>");
		out.write("<P>Starting work...</P>");
		for (int j = 1; j < 3; ++j) {
			for (int i = 0; i < Integer.MAX_VALUE; ++i) {
			}
		}
		out.write("<P>Done!</P>");
		out.write("</BODY></HTML>");
	}
}