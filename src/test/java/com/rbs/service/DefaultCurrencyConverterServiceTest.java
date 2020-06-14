package com.rbs.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DefaultCurrencyConverterService.class})
class DefaultCurrencyConverterServiceTest {

    @Autowired
    private CurrencyConverterService currencyConverterService;

    @Test
    void convert() {
        final var originalAmount = BigDecimal.TEN;
        final var convertedAmount = currencyConverterService.convert(originalAmount, "USD", "JPY");
        assertNotNull(convertedAmount);
        assertTrue(convertedAmount.compareTo(originalAmount) > 0, "Amount in JPY must be > than amount in USD");
    }

    @Test
    void convertToTheSameCurrency() {
        final var originalAmount = BigDecimal.TEN;
        final var convertedAmount = currencyConverterService.convert(originalAmount, "USD", "uSd");
        assertNotNull(convertedAmount);
        assertSame(originalAmount, convertedAmount);
    }
}