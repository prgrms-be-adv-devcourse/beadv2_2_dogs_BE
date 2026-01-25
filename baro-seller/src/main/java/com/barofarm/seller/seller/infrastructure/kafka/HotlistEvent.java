package com.barofarm.seller.seller.infrastructure.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// [0] Auth에서 발행하는 hotlist 이벤트를 역직렬화하는 DTO.
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotlistEvent {

    // [1] 이벤트 식별자(중복 처리/추적 용도).
    private String eventId;
    // [2] 대상 타입(user/seller 등).
    private String subjectType;
    // [3] 대상 식별자(UUID 문자열).
    private String subjectId;
    // [4] 활성 여부(여기서는 참고용).
    private Boolean active;
    // [5] 상태 값(APPROVED/REJECTED/SUSPENDED).
    private String status;
    // [6] 추가 플래그(사용하지 않지만 구조 유지).
    private List<String> flags;
    // [7] 변경 사유.
    private String reason;
    // [8] 변경 시각(ISO-8601).
    private String updatedAt;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
