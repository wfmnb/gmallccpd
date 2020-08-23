package com.ccpd.gmall0822.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
//不是兄弟关系时使用该注解
@ComponentScan
//指定Mapper路径
@MapperScan(basePackages = "com.ccpd.gmall0822.user.mapper")
public class Gmall0822UserManageApplication {

	public static void main(String[] args) {
		SpringApplication.run(Gmall0822UserManageApplication.class, args);
	}

}
