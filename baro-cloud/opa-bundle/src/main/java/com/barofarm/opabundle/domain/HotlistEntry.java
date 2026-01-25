package com.barofarm.opabundle.domain;

import java.util.List;

// [0] OPA 데이터에 포함되는 hotlist 항목 도메인 모델 클래스.
public class HotlistEntry {

    // [1] Whether the subject is currently hotlisted.
    private boolean active;
    // [1-1] Status used by Rego checks (ACTIVE/SUSPENDED/BLOCKED).
    private String status;
    // [2] Flags applied to the subject at decision time.
    private List<String> flags;
    // [3] Reason for the hotlist state.
    private String reason;
    // [4] ISO-8601 timestamp of the last update.
    private String updatedAt;

    public HotlistEntry() {
    }

    public HotlistEntry(boolean active, List<String> flags, String reason, String updatedAt) {
        this.active = active;
        this.flags = flags;
        this.reason = reason;
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
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
