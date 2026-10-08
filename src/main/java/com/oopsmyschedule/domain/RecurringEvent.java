package com.oopsmyschedule.domain;

import java.time.LocalTime;

/**
 * Việc lặp lại mỗi ngày: tập thể dục, đọc sách mỗi tối, học tiếng Anh...
 * Khi bị trễ, ưu tiên bỏ qua hôm nay ({@link #boQuaHomNay()}) hơn là chèn ép
 * lịch — vì bản chất của recurring event là "làm đều đặn", bỏ 1 hôm không
 * phá vỡ toàn bộ kế hoạch dài hạn, chỉ reset streak của riêng hôm đó.
 */
public class RecurringEvent extends ScheduleItem {

    private int soNgayLienTiep;
    private boolean boQuaHomNay = false;

    public RecurringEvent(String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                           PriorityLevel doUuTien, int soNgayLienTiep) {
        super(ten, gioBatDau, gioKetThuc, doUuTien);
        this.soNgayLienTiep = soNgayLienTiep;
    }

    /** Copy constructor giữ nguyên id + trạng thái (xem javadoc ở ScheduleItem). */
    public RecurringEvent(String id, String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                           PriorityLevel doUuTien, boolean hoanThanh, int soNgayLienTiep, boolean boQuaHomNay) {
        super(id, ten, gioBatDau, gioKetThuc, doUuTien, hoanThanh);
        this.soNgayLienTiep = soNgayLienTiep;
        this.boQuaHomNay = boQuaHomNay;
    }

    @Override
    public boolean coTheDoiLich() {
        // Recurring event chấp nhận bị dời nhẹ trong ngày; nếu không đủ chỗ
        // thì ReplanEngine sẽ gọi boQuaHomNay() thay vì ép dời (xem xuLyKhiBiTre).
        return true;
    }

    @Override
    public void xuLyKhiBiTre(int soPhutTre) {
        doiLich(soPhutTre);
    }

    /** Bỏ hẳn hôm nay — không ảnh hưởng đến chuỗi ngày khác trong lịch sử, chỉ reset streak hôm nay. */
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
