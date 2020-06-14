package com.rbs.controller;

import com.rbs.model.Bookings;
import com.rbs.service.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/client/bookings")
    Bookings getBookingsForClient() {
        // TODO: Get client id from the auth context as soon as security filter is added
        return bookingService.getBookingsForClient(UUID.fromString("f14e9d07-a7f8-42bc-87e6-be8d1ffde7d1"));
    }

}
