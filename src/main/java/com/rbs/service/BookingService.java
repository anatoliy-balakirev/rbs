package com.rbs.service;

import com.rbs.model.Amount;
import com.rbs.model.Booking;
import com.rbs.model.Bookings;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    public Bookings getBookingsForClient(final UUID clientId) {
        return Bookings.builder()
                .bookings(List.of(booking("some booking 1", clientId), booking("some booking 2", clientId)))
                .total(amount())
                .build();
    }

    private static Booking booking(final String description, final UUID clientId) {
        return Booking.builder().clientId(clientId)
                .creationTime(ZonedDateTime.now()).description(description).amount(amount()).build();
    }

    private static Amount amount() {
        return Amount.builder().value(BigDecimal.TEN).currency("GBP").build();
    }
}
