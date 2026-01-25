package com.barofarm.opabundle.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
// [0] Kafka로 전달되는 hotlist 변경 이벤트의 DTO 클래스.
public class HotlistEvent {

    // [0] Optional event id for idempotent processing.
    private String eventId;
    // [1] Logical subject type (e.g. user, seller) for hotlist lookup.
    private String subjectType;
    // [2] Subject identifier within the type.
    private String subjectId;
    // [3] True to add/update, false to remove from the hotlist.
    private Boolean active;
    // [3-1] Optional status override (ACTIVE/SUSPENDED/BLOCKED).
    private String status;
    // [4] Optional flags applied to the subject.
    private List<String> flags;
    // [5] Optional reason for auditing/debugging.
    private String reason;
    // [6] ISO-8601 timestamp for event freshness.
    private String updatedAt;

    public String getSubjectType() {
        return subjectType;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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
