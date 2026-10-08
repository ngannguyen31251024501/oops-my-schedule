package com.oopsmyschedule.domain;

/**
 * Mức độ ưu tiên của một {@link ScheduleItem}.
 * ReplanEngine dùng enum này để quyết định "việc nào bị hy sinh trước"
 * khi lịch không còn đủ chỗ cho tất cả các việc sau sự cố.
 */
public enum PriorityLevel {
    BAT_BUOC(3, "Bắt buộc"),
    NEN_LAM(2, "Nên làm"),
    CO_THE_BO(1, "Có thể bỏ");

    private final int trongSo;
    private final String nhan;

    PriorityLevel(int trongSo, String nhan) {
        this.trongSo = trongSo;
        this.nhan = nhan;
    }

    /** Trọng số càng cao thì càng "cứng", càng khó bị hy sinh. */
    public int getTrongSo() {
        return trongSo;
    }

    public String getNhan() {
        return nhan;
    }

    @Override
    public String toString() {
        return nhan;
    }
}
