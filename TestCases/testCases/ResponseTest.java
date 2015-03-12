package testCases;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis.cis455.webserver.FakeRequest;
import edu.upenn.cis.cis455.webserver.FakeResponse;
import edu.upenn.cis.cis455.webserver.HttpRequest;




public class ResponseTest {
	HttpRequest httpRequest;
	FakeRequest servletRequest;
	FakeResponse servletResponse;
	
	@Before
	public void init() throws Exception
	{
		String CRLF ="\r\n";
	    String requestUrl = "GET /Hello?a=b&d=e HTTP/1.1";
	    String requestHeaders = "Host: localhost:8000" +CRLF+
	            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8" +CRLF+
	            "Accept-Language: en-us,en;q=0.5" +CRLF+
	            "Accept-Encoding: gzip,deflate" +CRLF+CRLF;
	    DataOutputStream out = new DataOutputStream(null);
	    
	    InputStream stream = new ByteArrayInputStream(requestHeaders.getBytes(StandardCharsets.UTF_8));
	    InputStreamReader reader = new InputStreamReader(stream);
	    BufferedReader bufferedReader = new BufferedReader(reader);
	    java.net.Socket socket = new Socket();
	    
	    httpRequest = new HttpRequest(bufferedReader, "GET", "HTTP/1.1", 8000, socket);
	    
	    servletResponse = new FakeResponse(out, httpRequest);
	    servletRequest = new FakeRequest(null, httpRequest, servletResponse);
	}

	@Test
	public void testContainsHeader() {
		servletResponse.addHeader("Location", "http://www.google.com");
		assertEquals(servletResponse.containsHeader("location"), true);
	}

	@Test
	public void testAddDateHeader() throws ParseException {
		SimpleDateFormat sdf =
                new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		servletResponse.addDateHeader("Date", sdf.parse("Fri, 31 Dec 1999 23:59:59 GMT").getTime()) ;
		assertEquals(servletResponse.headers.get("date"), "Fri, 31 Dec 1999 23:59:59 GMT");
	}
	
	@Test
	public void setStatusCode() {
		servletResponse.setStatus(200);
		assertEquals(servletResponse.statusCode, 200);
	}
}

