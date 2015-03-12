package testCases;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Context;

import edu.upenn.cis.cis455.webserver.FakeContext;

public class ContextTest {

	FakeContext context;
	@Before
	public void init()
	{
		context = new FakeContext();
	}
	
	@Test
	public void test() {
		context.setAttribute("name", "value");
		assertEquals(context.getAttribute("name"),"value");
	}	

}
