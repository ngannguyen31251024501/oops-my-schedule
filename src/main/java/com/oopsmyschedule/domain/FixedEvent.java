package com.oopsmyschedule.domain;

import java.time.LocalTime;

/**
 * Việc cố định: giờ học, cuộc họp, giờ đi (xe từ quê lên Sài Gòn)...
 * Bản chất KHÔNG BAO GIỜ được dời, bất kể mức độ ưu tiên là gì.
 */
public class FixedEvent extends ScheduleItem {

    public FixedEvent(String ten, LocalTime gioBatDau, LocalTime gioKetThuc, PriorityLevel doUuTien) {
        super(ten, gioBatDau, gioKetThuc, doUuTien);
    }

    /** Copy constructor giữ nguyên id + trạng thái hoàn thành (xem javadoc ở ScheduleItem). */
    public FixedEvent(String id, String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                       PriorityLevel doUuTien, boolean hoanThanh) {
        super(id, ten, gioBatDau, gioKetThuc, doUuTien, hoanThanh);
    }

    @Override
    public boolean coTheDoiLich() {
        return false;
    }

    @Override
    public void xuLyKhiBiTre(int soPhutTre) {
        // Đúng như thiết kế: gọi dời lịch trên 1 FixedEvent là lỗi logic của
        // người gọi (ReplanEngine/Disruption phải kiểm tra coTheDoiLich()
        // trước). Ném lỗi ở đây để bắt lỗi sớm thay vì âm thầm sai dữ liệu.
        throw new IllegalStateException(
            "Không thể dời lịch cho việc cố định \"" + getTen() + "\" — hãy kiểm tra coTheDoiLich() trước khi gọi.");
    }

    @Override
    public String moTaTrangThai() {
        return "Giữ nguyên (việc cố định, không thể dời)";
    }

    @Override
    public String loaiViec() {
        return "Cố định";
    }
}
