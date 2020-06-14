package com.rbs.service;

import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;

import javax.money.Monetary;
import javax.money.convert.MonetaryConversions;
import java.math.BigDecimal;

/**
 * Default converter service, using javax.money package.
 */
// TODO: This converter is here to provide something as a default. In real code proper converter must be used instead.
@Service
public class DefaultCurrencyConverterService implements CurrencyConverterService {

    // TODO: Review performance of this method.
    @Override
    public BigDecimal convert(final BigDecimal amount, final String fromCurrency, final String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }
        final var originalAmount = Monetary.getDefaultAmountFactory()
                .setAmount(Money.of(amount, Monetary.getCurrency(fromCurrency))).create();

        return Money.from(originalAmount.with(MonetaryConversions.getConversion(toCurrency))).getNumberStripped();
    }
}
