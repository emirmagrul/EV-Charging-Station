package com.ev.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = "com.ev")
@EnableJpaRepositories(basePackages = "com.ev.repository")
@EntityScan(basePackages = "com.ev.model")
public class Application {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Istanbul"));
    }

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
