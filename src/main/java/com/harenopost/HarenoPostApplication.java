package com.harenopost;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HarenoPostApplication {

	public static void main(String[] args) {
		// -Duser.timezone=Asia/Tokyo 를 빠뜨려도 JST 로 동작하게
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
		SpringApplication.run(HarenoPostApplication.class, args);
	}

}
