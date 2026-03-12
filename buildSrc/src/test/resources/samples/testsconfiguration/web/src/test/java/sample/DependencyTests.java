package sample;

import org.junit.jupiter.api.Test;

public class DependencyTests {
	@Test
	public void findsDependencyOnClasspath() {
		new Dependency();
	}
}
