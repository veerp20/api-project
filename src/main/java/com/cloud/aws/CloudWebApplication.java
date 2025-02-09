package com.cloud.aws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.cloud.aws.repository")
public class CloudWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudWebApplication.class, args);
	}

}
