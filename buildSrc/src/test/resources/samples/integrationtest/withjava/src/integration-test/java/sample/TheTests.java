package sample;

import org.junit.Test;

import org.springframework.core.Ordered;

public class TheTests {
	@Test
	public void compilesAndRuns() {
		Ordered ordered = new Ordered() {
			@Override
			public int getOrder() {
				return 0;
			}
		};
	}
}
