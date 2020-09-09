package com.ccpd.gmall0822.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan("com.ccpd.gmall0822")
@MapperScan("com.ccpd.gmall0822.cart.mapper")
public class Gmall0822CartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0822CartServiceApplication.class, args);
	}

}
