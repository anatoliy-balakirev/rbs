package com.rbs.controller;

import com.rbs.model.Bookings;
import com.rbs.service.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/client/bookings")
    Bookings getBookingsForClient(@AuthenticationPrincipal final UUID clientId) {
        return bookingService.getBookingsForClient(clientId);
    }

}
