package com.ccpd.gmall0822.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.ccpd.gmall0822.manage.mapper")
@EnableTransactionManagement//启用事务
public class Gmall0822ManageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0822ManageServiceApplication.class, args);
	}

}
