package testCases;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.upenn.cis.cis455.webserver.FakeSession;

public class SessionTest {

	FakeSession session;
	@Before
	public void init()
	{
		session = new FakeSession();
	}
	
	@Test
	public void testGetCreationTime() {
		assertNotEquals(session.getCreationTime(), null);
	}

	@Test
	public void testGetId() {
		assertNotEquals(session.getId(), null);
	}

	@Test
	public void testGetAttribute() {
		session.setAttribute("attr1", "val1");
		session.setAttribute("attr2", "val2");
		
		assertEquals("val1", session.getAttribute("attr1"));
	}

}
