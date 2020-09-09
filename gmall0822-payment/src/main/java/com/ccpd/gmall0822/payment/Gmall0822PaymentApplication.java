package com.ccpd.gmall0822.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan("com.ccpd.gmall0822")
@MapperScan("com.ccpd.gmall0822.payment.mapper")
public class Gmall0822PaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0822PaymentApplication.class, args);
	}

}
