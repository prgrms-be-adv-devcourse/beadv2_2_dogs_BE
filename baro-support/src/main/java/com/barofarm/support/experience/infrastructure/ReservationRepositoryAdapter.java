package com.barofarm.support.experience.infrastructure;

import com.barofarm.support.experience.domain.Reservation;
import com.barofarm.support.experience.domain.ReservationRepository;
import com.barofarm.support.experience.domain.ReservationStatus;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/** 예약 리포지토리 어댑터 (Adapter) */
@Repository
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final ReservationJpaRepository jpaRepository;

    @Override
    public Reservation save(Reservation reservation) {
        return jpaRepository.save(reservation);
    }

    @Override
    public Optional<Reservation> findById(UUID reservationId) {
        return jpaRepository.findById(reservationId);
    }

    @Override
    public boolean existsById(UUID reservationId) {
        return jpaRepository.existsById(reservationId);
    }

    @Override
    public void deleteById(UUID reservationId) {
        jpaRepository.deleteById(reservationId);
    }

    @Override
    public int sumHeadCountByExperienceIdAndReservedDateAndReservedTimeSlot(
        UUID experienceId,
        LocalDate reservedDate,
        String reservedTimeSlot
    ) {
        return jpaRepository.sumHeadCountByExperienceIdAndReservedDateAndReservedTimeSlot(
            experienceId,
            reservedDate,
            reservedTimeSlot,
            ReservationStatus.REQUESTED,
            ReservationStatus.CONFIRMED
        );
    }

    @Override
    public Page<Reservation> findByExperienceId(UUID experienceId, Pageable pageable) {
        return jpaRepository.findByExperienceId(experienceId, pageable);
    }

    @Override
    public Page<Reservation> findByBuyerId(UUID buyerId, Pageable pageable) {
        return jpaRepository.findByBuyerId(buyerId, pageable);
    }
}
