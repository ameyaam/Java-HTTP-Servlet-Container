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

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis.cis455.webserver.FakeRequest;
import edu.upenn.cis.cis455.webserver.FakeResponse;
import edu.upenn.cis.cis455.webserver.HttpRequest;

public class RequestTest {

	HttpRequest httpRequest;
	FakeRequest servletRequest;
	FakeResponse response;
	@Before
	public void init() throws Exception
	{
		String CRLF ="\r\n";
        String requestUrl = "GET /Hello?a=b&d=e HTTP/1.1";
        String requestHeaders = "Host: localhost:8000" +CRLF+
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8" +CRLF+
                "Accept-Language: en-us,en;q=0.5" +CRLF+
                "Cookie: name=value" +CRLF+
                "Date: Fri, 31 Dec 1999 23:59:59 GMT" + CRLF +
                "Accept-Encoding: gzip,deflate" +CRLF+CRLF;
        DataOutputStream out = new DataOutputStream(null);
        
        InputStream stream = new ByteArrayInputStream(requestHeaders.getBytes(StandardCharsets.UTF_8));
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        java.net.Socket socket = new Socket();
        
        httpRequest = new HttpRequest(bufferedReader, "GET", "HTTP/1.1", 8000, socket);
        
        response = new FakeResponse(out, httpRequest);
        servletRequest = new FakeRequest(null, httpRequest, response);
        
	}	
	
	@Test
	public void testGetMethod() {
		assertEquals(servletRequest.getMethod(), "GET");
	}
	
	@Test
	public void testGetAuthType() {
		assertEquals(servletRequest.getAuthType(), "BASIC");
	}
	
	@Test
	public void testgetCookies() {
		Cookie[] cookies = servletRequest.getCookies();
		for(Cookie cookie:cookies) {
			assertEquals(cookie.getName(), "name");
			assertEquals(cookie.getValue(), "value");
		}
	}
	
	@Test
	public void testgetDateHeader() throws ParseException {
		SimpleDateFormat sdf =
                new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		assertEquals(servletRequest.getDateHeader("date"), sdf.parse("Fri, 31 Dec 1999 23:59:59 GMT").getTime());
	}
	
}
