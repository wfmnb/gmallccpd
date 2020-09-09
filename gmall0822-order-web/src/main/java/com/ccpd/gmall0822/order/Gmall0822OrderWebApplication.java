package com.ccpd.gmall0822.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.ccpd.gmall0822")
public class Gmall0822OrderWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0822OrderWebApplication.class, args);
	}

}
