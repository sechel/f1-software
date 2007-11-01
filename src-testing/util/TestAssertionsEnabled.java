package util;
import junit.framework.TestCase;

public class TestAssertionsEnabled extends TestCase {

	public void test() {
		boolean enabled = false;
		assert enabled = true;
		assertTrue("Please enable assertions.", enabled);
	}
	
}
