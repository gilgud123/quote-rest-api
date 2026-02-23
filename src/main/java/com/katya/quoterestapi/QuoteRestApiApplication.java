package com.katya.quoterestapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Quote REST API.
 * This Spring Boot application provides RESTful APIs for managing quotes and authors.
 */
@SpringBootApplication
public class QuoteRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuoteRestApiApplication.class, args);
    }
}
