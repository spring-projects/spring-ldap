package sample;

import org.junit.Test;

public class DependencyTests {
	@Test
	public void findsDependencyOnClasspath() {
		new Dependency();
	}
}
