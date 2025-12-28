package com.chauhan.linkedInProject.connection_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ConnectionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConnectionServiceApplication.class, args);
	}

}
