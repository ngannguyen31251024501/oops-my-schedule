package com.oopsmyschedule.domain;

import java.time.LocalTime;

/**
 * Việc lặp lại mỗi ngày: tập thể dục, đọc sách mỗi tối, học tiếng Anh...
 * Khi bị trễ, ưu tiên bỏ qua hôm nay ({@link #boQuaHomNay()}) hơn là chèn ép lịch.
 */
public class RecurringEvent extends ScheduleItem {

    private int soNgayLienTiep;
    private boolean boQuaHomNay = false;

    public RecurringEvent(String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                          PriorityLevel doUuTien, int soNgayLienTiep) {
        super(ten, gioBatDau, gioKetThuc, doUuTien);
        kiemTraSoNgay(soNgayLienTiep);
        this.soNgayLienTiep = soNgayLienTiep;
    }

    /** Copy constructor giữ nguyên id + trạng thái. */
    public RecurringEvent(String id, String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                          PriorityLevel doUuTien, boolean hoanThanh, int soNgayLienTiep, boolean boQuaHomNay) {
        super(id, ten, gioBatDau, gioKetThuc, doUuTien, hoanThanh);
        kiemTraSoNgay(soNgayLienTiep);
        this.soNgayLienTiep = soNgayLienTiep;
        this.boQuaHomNay = boQuaHomNay;
    }

    /** INV-14: số ngày liên tiếp không âm. */
    private static void kiemTraSoNgay(int soNgay) {
        if (soNgay < 0) {
            throw new IllegalArgumentException("Số ngày liên tiếp không được âm: " + soNgay);
        }
    }

    @Override
    public boolean coTheDoiLich() {
        return true;
    }

    /**
     * @throws IllegalStateException nếu hôm nay đã bỏ qua (INV-19) hoặc việc đã hoàn thành (INV-18)
     */
    @Override
    public void xuLyKhiBiTre(int soPhutTre) {
        if (boQuaHomNay) {
            throw new IllegalStateException(
                    "Việc \"" + getTen() + "\" đã bỏ qua hôm nay, không thể dời tiếp.");
        }
        doiLich(soPhutTre);
    }

    /** Bỏ hẳn hôm nay — chỉ reset streak hôm nay, không ảnh hưởng các ngày khác. */
    public void boQuaHomNay() {
        this.boQuaHomNay = true;
        this.soNgayLienTiep = 0;
    }

    public boolean isBoQuaHomNay() {
        return boQuaHomNay;
    }

    public int getSoNgayLienTiep() {
        return soNgayLienTiep;
    }

    @Override
    public String moTaTrangThai() {
        return boQuaHomNay ? "Đã bỏ qua hôm nay (streak reset)" : "Streak: " + soNgayLienTiep + " ngày";
    }

    @Override
    public String loaiViec() {
        return "Lặp lại";
    }
}