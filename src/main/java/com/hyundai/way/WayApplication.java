package com.hyundai.way;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class WayApplication {

	public static void main(String[] args) {
		SpringApplication.run(WayApplication.class, args);
	}

}
