package com.rbs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(value = "bookings")
public class BookingsProperties {
    private String defaultCurrencyForTotalAmount;
}
