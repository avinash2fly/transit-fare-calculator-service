package com.demo.transitfarecalculatorservice.fare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FareCalculatorTest {

    private FareCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new FareConfig().fareCalculator();
    }

    @Test
    void fareIsTheSameInBothDirections() {
        assertEquals(new BigDecimal("3.25"), calculator.fareBetween("Stop1", "Stop2"));
        assertEquals(new BigDecimal("3.25"), calculator.fareBetween("Stop2", "Stop1"));
        assertEquals(new BigDecimal("5.50"), calculator.fareBetween("Stop2", "Stop3"));
        assertEquals(new BigDecimal("7.30"), calculator.fareBetween("Stop1", "Stop3"));
    }

    @Test
    void maxFareFromAStopIsTheMostExpensiveRouteFromThatStop() {
        assertEquals(new BigDecimal("7.30"), calculator.maxFareFrom("Stop1"));
        assertEquals(new BigDecimal("5.50"), calculator.maxFareFrom("Stop2"));
        assertEquals(new BigDecimal("7.30"), calculator.maxFareFrom("Stop3"));
    }
}
