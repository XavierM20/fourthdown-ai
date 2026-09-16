package com.fourthdown.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		properties = {
				"CFBD_API_KEY=test-key"
		}
)
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}
}