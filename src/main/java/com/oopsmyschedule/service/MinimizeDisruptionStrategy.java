package com.oopsmyschedule.service;

/**
 * Phương án 1 — "Ít thay đổi": cố giữ càng nhiều việc ở vị trí cũ càng tốt,
 * chỉ bỏ hẳn việc nào thật sự "Có thể bỏ" khi hết chỗ. Đây là hành vi gốc
 * (mặc định) mà {@code ReplanEngine} luôn dùng trước khi có 3 phương án.
 */
public class MinimizeDisruptionStrategy extends AbstractReplanStrategy {

    @Override
    public String getTen() {
        return "Ít thay đổi";
    }

    @Override
    public String getBieuTuong() {
        return "⭐";
    }

    @Override
    public String getMoTaNgan() {
        return "Giữ lại nhiều hoạt động nhất, chỉ dời hoặc bỏ những gì thật sự không còn chỗ.";
    }
}
