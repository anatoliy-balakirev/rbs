package com.rbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbs.config.BookingsProperties;
import com.rbs.data.entity.BookingEntity;
import com.rbs.data.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BookingService.class, BookingServiceTest.TestConfig.class,
        JacksonAutoConfiguration.class},
        initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(BookingsProperties.class)
class BookingServiceTest {

    private static final UUID CLIENT_ID = UUID.fromString("e4473f24-55e6-4e7b-b11a-8211744fbdfe");
    private static final ZonedDateTime NOW =
            ZonedDateTime.from(ISO_OFFSET_DATE_TIME.parse("2020-06-14T10:54:27.6374357+02:00"));
    private static final Instant BEGINNING_OF_MONTH =
            ZonedDateTime.from(ISO_OFFSET_DATE_TIME.parse("2020-06-01T00:00:00Z")).toInstant();
    private static final Instant END_OF_MONTH =
            ZonedDateTime.from(ISO_OFFSET_DATE_TIME.parse("2020-06-30T23:59:59Z")).toInstant();

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingsProperties properties;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private CurrencyConverterService currencyConverterService;

    @BeforeEach
    void setUp() {
        when(currencyConverterService.convert(any(), anyString(), anyString())).thenAnswer(invocation -> {
            final var amount = invocation.getArgument(0, BigDecimal.class);
            final var fromCurrency = invocation.getArgument(1, String.class);
            final var toCurrency = invocation.getArgument(2, String.class);
            if (fromCurrency.equalsIgnoreCase(toCurrency)) {
                return amount;
            } else {
                return amount.add(BigDecimal.ONE);
            }
        });
    }

    @Test
    void getCurrentMonthBookingsForClient() throws Exception {
        final var booking1 = BookingEntity.builder().creationTime(NOW.toInstant()).amount(BigDecimal.valueOf(19))
                .currency("GBP").clientId(CLIENT_ID).description("Some description 1").build();
        final var booking2 = BookingEntity.builder().creationTime(NOW.plusDays(1).toInstant()).amount(BigDecimal.ONE)
                .currency("USD").clientId(CLIENT_ID).description("Some description 2").build();

        when(bookingRepository.findAllByClientIdAndCreationTimeBetween(any(), any(), any()))
                .thenReturn(List.of(booking1, booking2));

        final var bookings = bookingService.getCurrentMonthBookingsForClient(CLIENT_ID);
        final var actualBookingsJson = objectMapper.writeValueAsString(bookings);
        final var expectedBookingsJson =
                Files.readString(Paths.get(BookingServiceTest.class.getResource("/bookings/bookings.json").toURI()));

        JSONAssert.assertEquals(expectedBookingsJson, actualBookingsJson, true);
    }

    @Test
    void getCurrentMonthBookingsForClientWithoutBookings() {
        when(bookingRepository.findAllByClientIdAndCreationTimeBetween(any(), any(), any())).thenReturn(List.of());

        final var bookings = bookingService.getCurrentMonthBookingsForClient(CLIENT_ID);
        assertNotNull(bookings);
        assertEquals("GBP", bookings.getTotal().getCurrency());
        assertEquals(BigDecimal.ZERO, bookings.getTotal().getValue());
        assertTrue(bookings.getBookings().isEmpty(), "Bookings must be empty");

        verify(bookingRepository)
                .findAllByClientIdAndCreationTimeBetween(eq(CLIENT_ID), eq(BEGINNING_OF_MONTH), eq(END_OF_MONTH));
        verifyNoInteractions(currencyConverterService);
    }

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public Clock clock() {
            return Clock.fixed(NOW.toInstant(), ZoneOffset.UTC);
        }
    }
}