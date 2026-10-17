package com.demo.transitfarecalculatorservice;

import org.springframework.boot.SpringApplication;

public class TestTransitFareCalculatorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(TransitFareCalculatorServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
