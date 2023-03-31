package sample;

import org.junit.*;

public class DependencyTests {
	@Test
	public void findsDependencyOnClasspath() {
		new Dependency();
	}
}
