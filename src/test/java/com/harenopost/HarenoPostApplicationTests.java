package com.harenopost;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Oracle 접속 정보(application-secret.yml)가 있어야 한다. DB 연결 후 직접 실행")
class HarenoPostApplicationTests {

	@Test
	void contextLoads() {
	}

}
