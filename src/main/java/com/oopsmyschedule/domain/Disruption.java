package com.oopsmyschedule.domain;

import java.util.List;

/**
 * Lớp cha trừu tượng cho 1 sự cố phát sinh trong ngày (trễ giờ, hoặc phải bỏ
 * 1 việc). Đây là vế thứ hai của "đa hình kép" trong đồ án: cùng là
 * {@code Disruption.apDung(...)} nhưng {@link DelayDisruption} và
 * {@link SkipDisruption} phản ứng hoàn toàn khác nhau trên cùng 1 danh sách
 * {@link ScheduleItem}.
 * <p>
 * Lưu ý phân tầng trách nhiệm: {@code apDung()} chỉ làm hành động "máy móc"
 * cấp item (dời từng item còn dời được, hoặc xóa item bị bỏ). Việc phân
 * tích ưu tiên, giải quyết xung đột giữa nhiều item khi không đủ chỗ là
 * trách nhiệm của {@code ReplanEngine} ở tầng cao hơn — không trộn 2 việc
 * này vào 1 lớp để giữ Single Responsibility.
 */
public abstract class Disruption {

    private final String moTa;

    protected Disruption(String moTa) {
        this.moTa = moTa;
    }

    /** Áp dụng sự cố này lên danh sách các việc còn lại trong ngày (mutate tại chỗ). */
    public abstract void apDung(List<ScheduleItem> danhSachConLai);

    public String getMoTa() {
        return moTa;
    }
}
