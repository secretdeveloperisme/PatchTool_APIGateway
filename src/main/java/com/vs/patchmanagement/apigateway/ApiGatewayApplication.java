package com.vs.patchmanagement.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		String command = "start";
		if (args.length > 0) {
			command = args[args.length - 1];
		}
		switch (command) {
			case "start":
				SpringApplication.run(ApiGatewayApplication.class, args);
				break;
			case "stop":
				System.exit(0);
				break;
			default:
		}
	}

}
