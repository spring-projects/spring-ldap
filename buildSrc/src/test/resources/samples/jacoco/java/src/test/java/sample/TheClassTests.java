package sample;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TheClassTests {
	TheClass theClass = new TheClass();

	@Test
	public void doStuffWhenTrueThenTrue() {
		assertTrue(theClass.doStuff(true));
	}

	@Test
	public void doStuffWhenTrueThenFalse() {
		assertFalse(theClass.doStuff(false));
	}
}
