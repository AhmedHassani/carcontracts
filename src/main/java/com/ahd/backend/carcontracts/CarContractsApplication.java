package com.ahd.backend.carcontracts;

import com.ahd.backend.carcontracts.config.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(JwtProperties.class)
public class CarContractsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarContractsApplication.class, args);
	}

}
