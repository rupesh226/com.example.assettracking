package com.example.assettracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CrudDynamoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrudDynamoApplication.class, args);
	}

}
