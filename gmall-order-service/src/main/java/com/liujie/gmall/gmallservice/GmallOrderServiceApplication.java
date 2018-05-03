package com.liujie.gmall.gmallservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.liujie.gmall.gmallservice.mapper")
@ComponentScan(basePackages = "com.liujie.gmall")
public class GmallOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallOrderServiceApplication.class, args);
	}
}
