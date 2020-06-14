package com.rbs.data.repository;

import com.rbs.data.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

    List<BookingEntity> findAllByClientId(UUID clientId);

    List<BookingEntity> findAllByClientIdAndCreationTimeBetween(UUID clientId, Instant startTime, Instant endTime);
}
