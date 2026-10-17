package com.demo.transitfarecalculatorservice.fare;

import java.math.BigDecimal;

public record Route(
        String stopA,
        String stopB,
        BigDecimal fare) {
}
