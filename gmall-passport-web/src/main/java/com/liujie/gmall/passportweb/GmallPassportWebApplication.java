package com.liujie.gmall.passportweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.liujie.gmall")
public class GmallPassportWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallPassportWebApplication.class, args);
	}
}
