package com.rbs.service;

import java.math.BigDecimal;

/**
 * Service to convert amount from one currency to another.
 */
public interface CurrencyConverterService {

    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency);
}
