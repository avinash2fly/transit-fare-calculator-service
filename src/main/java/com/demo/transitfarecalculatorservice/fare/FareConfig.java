package com.demo.transitfarecalculatorservice.fare;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FareConfig {

    @Bean
    public FareCalculator fareCalculator() {
        return new FareCalculator(List.of(
                new Route("Stop1", "Stop2", new BigDecimal("3.25")),
                new Route("Stop2", "Stop3", new BigDecimal("5.50")),
                new Route("Stop1", "Stop3", new BigDecimal("7.30"))));
    }
}
