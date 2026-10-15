package com.oopsmyschedule.domain;

import java.time.LocalTime;

/**
 * Việc cố định: giờ học, cuộc họp, giờ đi xe...
 * Bản chất KHÔNG BAO GIỜ được dời và luôn ở mức BAT_BUOC (INV-17).
 */
public class FixedEvent extends ScheduleItem {

    /** INV-17: mức ưu tiên luôn bị ép về BAT_BUOC dù người gọi truyền gì. */
    public FixedEvent(String ten, LocalTime gioBatDau, LocalTime gioKetThuc, PriorityLevel doUuTien) {
        super(ten, gioBatDau, gioKetThuc, PriorityLevel.BAT_BUOC);
    }

    /** Copy constructor giữ nguyên id + trạng thái hoàn thành. */
    public FixedEvent(String id, String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                      PriorityLevel doUuTien, boolean hoanThanh) {
        super(id, ten, gioBatDau, gioKetThuc, PriorityLevel.BAT_BUOC, hoanThanh);
    }

    @Override
    public boolean coTheDoiLich() {
        return false;
    }

    @Override
    public void xuLyKhiBiTre(int soPhutTre) {
        throw new IllegalStateException(
                "Không thể dời lịch cho việc cố định \"" + getTen() + "\" — hãy kiểm tra coTheDoiLich() trước khi gọi.");
    }

    /** INV-20: chặn cả đường ReplanEngine (ganLaiKhungGioBoiEngine gọi vào hàm này). */
    @Override
    protected void datLaiKhungGio(LocalTime moiBatDau, LocalTime moiKetThuc) {
        throw new IllegalStateException(
                "Không thể đổi giờ việc cố định \"" + getTen() + "\".");
    }

    /** INV-17: không cho hạ mức ưu tiên của việc cố định sau khi tạo. */
    @Override
    public void setDoUuTien(PriorityLevel doUuTien) {
        if (doUuTien != PriorityLevel.BAT_BUOC) {
            throw new IllegalArgumentException("Việc cố định luôn phải ở mức BAT_BUOC.");
        }
        super.setDoUuTien(doUuTien);
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