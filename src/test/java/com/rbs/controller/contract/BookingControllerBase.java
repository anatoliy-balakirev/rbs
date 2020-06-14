package com.rbs.controller.contract;

import com.rbs.config.RbsConfiguration;
import com.rbs.config.security.SecurityConfig;
import com.rbs.controller.BookingController;
import com.rbs.controller.ExceptionHandler;
import com.rbs.model.Amount;
import com.rbs.model.Booking;
import com.rbs.model.Bookings;
import com.rbs.service.BookingService;
import com.rbs.service.JwtService;
import io.jsonwebtoken.SignatureException;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(BookingController.class)
@ContextConfiguration(classes = {BookingController.class, RbsConfiguration.class, SecurityConfig.class,
        ExceptionHandler.class})
public class BookingControllerBase {

    private static final UUID CLIENT_ID_OK = UUID.fromString("f14e9d07-a7f8-42bc-87e6-be8d1ffde7d1");
    private static final UUID CLIENT_ID_ERROR = UUID.fromString("f14e9d07-a7f8-42bc-87e6-be8d1ffde7d2");
    private static final ZonedDateTime NOW =
            ZonedDateTime.from(ISO_OFFSET_DATE_TIME.parse("2020-06-14T10:54:27.6374357+02:00"));
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        // Configuring auth resolver:
        when(jwtService.extractClientId(eq("valid_token"))).thenReturn(CLIENT_ID_OK);
        when(jwtService.extractClientId(eq("internal_error_token"))).thenReturn(CLIENT_ID_ERROR);
        when(jwtService.extractClientId(eq("invalid_token"))).thenThrow(new SignatureException("Invalid token"));

        // Configuring the service itself:
        when(bookingService.getCurrentMonthBookingsForClient(eq(CLIENT_ID_OK)))
                .thenReturn(createBookingsForClient());
        when(bookingService.getCurrentMonthBookingsForClient(eq(CLIENT_ID_ERROR)))
                .thenThrow(new RuntimeException("Expected exception for test"));
    }

    private static Bookings createBookingsForClient() {
        return Bookings.builder()
                .bookings(List.of(booking("some booking 1"), booking("some booking 2")))
                .total(amount())
                .build();
    }

    private static Booking booking(final String description) {
        return Booking.builder()
                .clientId(CLIENT_ID_OK)
                .creationTime(NOW)
                .description(description)
                .amount(amount())
                .build();
    }

    private static Amount amount() {
        return Amount.builder().value(BigDecimal.TEN).currency("GBP").build();
    }
}
