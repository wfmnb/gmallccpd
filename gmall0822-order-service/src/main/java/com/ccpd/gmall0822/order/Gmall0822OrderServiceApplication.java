package com.ccpd.gmall0822.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan("com.ccpd.gmall0822")
@MapperScan("com.ccpd.gmall0822.order.mapper")
@EnableTransactionManagement
public class Gmall0822OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0822OrderServiceApplication.class, args);
	}

}
