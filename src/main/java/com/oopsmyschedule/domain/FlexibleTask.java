package com.oopsmyschedule.domain;

import java.time.LocalTime;

/**
 * Việc linh hoạt: làm bài tập, đọc truyện giải trí, nấu cơm, học tiếng Anh,
 * tắm rửa, ăn uống... Có thể dời giờ trong ngày khi bị trễ.
 */
public class FlexibleTask extends ScheduleItem {

    private int soLanDaDoi = 0;

    public FlexibleTask(String ten, LocalTime gioBatDau, LocalTime gioKetThuc, PriorityLevel doUuTien) {
        super(ten, gioBatDau, gioKetThuc, doUuTien);
    }

    /** Copy constructor giữ nguyên id + trạng thái (xem javadoc ở ScheduleItem). */
    public FlexibleTask(String id, String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                         PriorityLevel doUuTien, boolean hoanThanh, int soLanDaDoi) {
        super(id, ten, gioBatDau, gioKetThuc, doUuTien, hoanThanh);
        this.soLanDaDoi = soLanDaDoi;
    }

    @Override
    public boolean coTheDoiLich() {
        return true;
    }

    @Override
    public void xuLyKhiBiTre(int soPhutTre) {
        doiLich(soPhutTre);
        soLanDaDoi++;
    }

    public int getSoLanDaDoi() {
        return soLanDaDoi;
    }

    @Override
    public String moTaTrangThai() {
        return soLanDaDoi == 0 ? "Chưa bị dời" : "Đã dời " + soLanDaDoi + " lần";
    }

    @Override
    public String loaiViec() {
        return "Linh hoạt";
    }
}
