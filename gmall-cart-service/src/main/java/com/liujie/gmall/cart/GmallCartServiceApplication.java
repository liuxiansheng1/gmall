package com.liujie.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.liujie.gmall.cart.mapper")
@ComponentScan(basePackages = "com.liujie.gmall")
public class GmallCartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallCartServiceApplication.class, args);
	}
}
