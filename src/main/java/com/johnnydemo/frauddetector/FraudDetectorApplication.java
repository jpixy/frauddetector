package com.johnnydemo.frauddetector;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class FraudDetectorApplication {

	@Value("${application.timezone:UTC}")
	private String applicationTimeZone;

	public static void main(String[] args) {
		SpringApplication.run(FraudDetectorApplication.class, args);
	}

	@PostConstruct
	public void executeAfterMain() {
		// 设置默认时区
		TimeZone.setDefault(TimeZone.getTimeZone(applicationTimeZone));
	}
}
