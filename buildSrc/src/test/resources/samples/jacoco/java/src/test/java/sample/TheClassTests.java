package sample;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
