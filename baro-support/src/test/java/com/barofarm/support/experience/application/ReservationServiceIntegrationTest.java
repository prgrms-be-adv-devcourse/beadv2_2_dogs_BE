package com.barofarm.support.experience.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.barofarm.exception.CustomException;
import com.barofarm.support.experience.application.dto.ReservationServiceRequest;
import com.barofarm.support.experience.application.dto.ReservationServiceResponse;
import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import com.barofarm.support.experience.domain.ExperienceStatus;
import com.barofarm.support.experience.domain.Reservation;
import com.barofarm.support.experience.domain.ReservationRepository;
import com.barofarm.support.experience.domain.ReservationStatus;
import com.barofarm.support.experience.exception.ReservationErrorCode;
import com.barofarm.support.experience.infrastructure.cache.FarmCacheService;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReservationService 통합 테스트
 * 동시성 제어(비관적 락, 낙관적 락) 검증
 *
 * 실제 DB, Redis 등 외부 의존성이 필요
 * 실행하려면 test 프로파일 설정과 테스트용 DB/Redis가 필요
 *
 * 통합 테스트 실행 방법:
 * 1. test 프로파일용 application-test.yml 설정
 * 2. 테스트용 DB 및 Redis 실행
 * 3. @SpringBootTest로 전체 컨텍스트 로드
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private FarmCacheService farmCacheService;

    private UUID experienceId;
    private UUID farmId;
    private UUID buyerId1;
    private UUID buyerId2;
    private Experience experience;

    @BeforeEach
    void setUp() {
        experienceId = UUID.randomUUID();
        farmId = UUID.randomUUID();
        buyerId1 = UUID.randomUUID();
        buyerId2 = UUID.randomUUID();

        // 체험 생성 (capacity: 10명)
        java.time.LocalDateTime availableStart = java.time.LocalDateTime.of(2025, 3, 1, 9, 0);
        java.time.LocalDateTime availableEnd = java.time.LocalDateTime.of(2025, 3, 31, 18, 0);
        experience = new Experience(experienceId, farmId, "Test Experience", "Description",
                15000L, 10, 120, availableStart, availableEnd, ExperienceStatus.ON_SALE);
        experienceRepository.save(experience);
    }

    @Test
    @DisplayName("동시에 예약을 생성하면 비관적 락으로 capacity 초과를 방지한다")
    void createReservation_ConcurrentCreation_PessimisticLock() throws InterruptedException {
        // given
        LocalDate reservedDate = LocalDate.of(2025, 3, 15);
        String reservedTimeSlot = "10:00-12:00";
        int threadCount = 5;
        int headCountPerThread = 3; // 각 스레드가 3명씩 예약 시도 (총 15명, capacity는 10명)

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            UUID buyerId = i % 2 == 0 ? buyerId1 : buyerId2;
            executor.submit(() -> {
                try {
                    ReservationServiceRequest request = new ReservationServiceRequest(
                            experienceId, buyerId, reservedDate, reservedTimeSlot,
                            headCountPerThread, (long) (15000 * headCountPerThread), null);
                    reservationService.createReservation(buyerId, request);
                    successCount.incrementAndGet();
                } catch (CustomException e) {
                    if (e.getErrorCode() == ReservationErrorCode.CAPACITY_EXCEEDED) {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        // 비관적 락으로 인해 정확히 capacity(10명)만큼만 예약되어야 함
        // 3명씩 예약하므로 최대 3개 예약만 성공 (3 + 3 + 3 = 9명) 또는 4개 예약 중 일부만 성공
        int totalReserved = reservationRepository
                .sumHeadCountByExperienceIdAndReservedDateAndReservedTimeSlot(
                        experienceId, reservedDate, reservedTimeSlot);

        assertThat(totalReserved).isLessThanOrEqualTo(10); // capacity 초과 방지 확인
        assertThat(successCount.get() + failureCount.get()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("동시에 예약 상태를 변경하면 낙관적 락으로 동시 수정을 감지한다")
    void updateReservationStatus_ConcurrentModification_OptimisticLock() throws InterruptedException {
        // given
        LocalDate reservedDate = LocalDate.of(2025, 3, 15);
        ReservationServiceRequest request = new ReservationServiceRequest(
                experienceId, buyerId1, reservedDate, "10:00-12:00",
                2, 30000L, null);
        ReservationServiceResponse created = reservationService.createReservation(buyerId1, request);
        UUID reservationId = created.getReservationId();

        UUID sellerId = UUID.randomUUID();
        // sellerId의 farmId를 Redis에 미리 설정 (권한 검증 통과를 위해)
        farmCacheService.updateCache(sellerId, farmId);

        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger concurrentModificationCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    reservationService.updateReservationStatus(sellerId, reservationId, ReservationStatus.CONFIRMED);
                    successCount.incrementAndGet();
                } catch (CustomException e) {
                    if (e.getErrorCode() == ReservationErrorCode.CONCURRENT_MODIFICATION) {
                        concurrentModificationCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // 기타 예외
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        // 낙관적 락으로 인해 하나만 성공하고 나머지는 CONCURRENT_MODIFICATION 예외 발생
        assertThat(successCount.get()).isEqualTo(1); // 하나만 성공
        assertThat(concurrentModificationCount.get()).isGreaterThanOrEqualTo(1); // 최소 하나는 동시 수정 감지

        // 최종 상태 확인
        Reservation finalReservation = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(finalReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
