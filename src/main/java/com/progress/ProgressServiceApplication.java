package com.progress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.progress"})
public class ProgressServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProgressServiceApplication.class, args);
	}

}
