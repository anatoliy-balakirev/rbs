package com.rbs.service;

import com.rbs.config.BookingsProperties;
import com.rbs.data.entity.BookingEntity;
import com.rbs.data.repository.BookingRepository;
import com.rbs.model.Amount;
import com.rbs.model.Booking;
import com.rbs.model.Bookings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final ZoneOffset TIME_ZONE = ZoneOffset.UTC;
    private final BookingsProperties properties;
    private final BookingRepository bookingRepository;
    private final CurrencyConverterService currencyConverterService;
    private final Clock clock;

    public Bookings getCurrentMonthBookingsForClient(final UUID clientId) {
        LOGGER.info("Getting current month bookings for client {}", clientId);

        final var bookings = getCurrentMonthBookings(clientId);
        final var totalAmountCurrency = properties.getDefaultCurrencyForTotalAmount();
        final var total = extractTotalAmountConvertingTo(bookings, totalAmountCurrency);

        LOGGER.info("Client {} has {} bookings in the current month", clientId, bookings.size());

        return Bookings.builder()
                .bookings(bookings.stream().map(BookingService::toBooking).collect(toList()))
                .total(toAmount(total, totalAmountCurrency))
                .build();
    }

    private List<BookingEntity> getCurrentMonthBookings(final UUID clientId) {
        final var now = ZonedDateTime.now(clock);
        final var beginningOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay().toInstant(TIME_ZONE);
        // SQL's 'between' is inclusive, so need to use last second of the last day here:
        final var endOfMonth =
                now.toLocalDate().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59).toInstant(TIME_ZONE);
        return bookingRepository.findAllByClientIdAndCreationTimeBetween(clientId, beginningOfMonth, endOfMonth);
    }

    private BigDecimal extractTotalAmountConvertingTo(final List<BookingEntity> bookings, final String currency) {
        return bookings.stream()
                // If original amount is already in the required currency - it will be returned as is:
                .map(entity -> currencyConverterService.convert(entity.getAmount(), entity.getCurrency(), currency))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static Booking toBooking(final BookingEntity entity) {
        return Booking.builder()
                .clientId(entity.getClientId())
                .creationTime(entity.getCreationTime().atZone(TIME_ZONE))
                .description(entity.getDescription())
                .amount(toAmount(entity.getAmount(), entity.getCurrency())).build();
    }

    private static Amount toAmount(final BigDecimal value, final String currency) {
        // This mapping doesn't set proper scale based on currency, so we might return, for example, 5 decimal places
        // for USD, which doesn't make sense:
        return Amount.builder().value(value).currency(currency).build();
    }
}
