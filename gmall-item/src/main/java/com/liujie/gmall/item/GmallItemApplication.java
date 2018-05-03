package com.liujie.gmall.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.liujie.gmall")
public class GmallItemApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallItemApplication.class, args);
	}
}
