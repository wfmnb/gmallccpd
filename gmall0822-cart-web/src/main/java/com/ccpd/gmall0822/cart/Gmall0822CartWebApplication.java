package com.ccpd.gmall0822.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.ccpd.gmall0822")
public class Gmall0822CartWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0822CartWebApplication.class, args);
	}

}
