package com.ahd.backend.carcontracts;

import com.ahd.backend.carcontracts.config.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class CarContractsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarContractsApplication.class, args);
	}

}
