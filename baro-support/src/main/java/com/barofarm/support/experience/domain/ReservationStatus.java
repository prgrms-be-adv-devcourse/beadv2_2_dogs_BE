package com.barofarm.support.experience.domain;

import java.util.Set;

/** 예약 상태 */
public enum ReservationStatus {
    REQUESTED,   // 예약 요청
    CONFIRMED,   // 예약 확정
    CANCELED,    // 예약 취소 (최종 상태)
    COMPLETED;   // 체험 완료 (최종 상태)

    // 전환이 가능한 상태 목록
    private static final Set<ReservationStatus> REQUESTED_TRANSITIONS = Set.of(CONFIRMED, CANCELED);
    private static final Set<ReservationStatus> CONFIRMED_TRANSITIONS = Set.of(COMPLETED, CANCELED);
    private static final Set<ReservationStatus> CANCELED_TRANSITIONS = Set.of();
    private static final Set<ReservationStatus> COMPLETED_TRANSITIONS = Set.of();

    /**
     * 현재 상태에서 지정된 상태로 전환 가능한지 확인
     *
     * @param newStatus 변경하려는 상태
     * @return 전환 가능 여부
     */
    public boolean canTransitionTo(ReservationStatus newStatus) {
        // 같은 상태로 변경하는 것은 허용
        if (this == newStatus) {
            return true;
        }

        // 최종 상태는 변경 불가
        if (isFinalStatus()) {
            return false;
        }

        // 허용된 전환 목록에 포함되어 있는지 확인
        return getAllowedTransitions().contains(newStatus);
    }

    /**
     * 현재 상태에서 전환 가능한 상태 목록 조회
     *
     * @return 전환 가능한 상태 목록
     */
    private Set<ReservationStatus> getAllowedTransitions() {
        return switch (this) {
            case REQUESTED -> REQUESTED_TRANSITIONS;
            case CONFIRMED -> CONFIRMED_TRANSITIONS;
            case CANCELED -> CANCELED_TRANSITIONS;
            case COMPLETED -> COMPLETED_TRANSITIONS;
        };
    }

    /**
     * 최종 상태인지 확인 (더 이상 변경 불가)
     *
     * @return 최종 상태 여부
     */
    public boolean isFinalStatus() {
        return this == CANCELED || this == COMPLETED;
    }
}
