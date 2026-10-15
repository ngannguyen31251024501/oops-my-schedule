package com.oopsmyschedule.controller;

import com.oopsmyschedule.domain.ScheduleItem;
import com.oopsmyschedule.service.DaySchedule;

import java.util.Objects;
import java.util.function.Function;

/**
 * Điều phối các thao tác thêm, sửa, xóa và hoàn thành công việc.
 * Controller không trực tiếp phụ thuộc vào MainFrame.
 */
public class ScheduleController {

    private final Function<java.time.LocalDate, DaySchedule> layLich;

    public ScheduleController(
            Function<java.time.LocalDate, DaySchedule> layLich) {
        this.layLich = Objects.requireNonNull(
                layLich, "Hàm lấy lịch không được null.");
    }

    public void themViec(
            ScheduleItem item,
            java.time.LocalDate ngay) {
        Objects.requireNonNull(item, "Công việc không được null.");
        DaySchedule lich = layLich.apply(
                Objects.requireNonNull(ngay, "Ngày không được null."));

        if (lich == null) {
            throw new IllegalStateException(
                    "Không tìm thấy lịch của ngày đã chọn.");
        }

        lich.themItem(item);
    }

    public void suaViec(
            ScheduleItem cu,
            java.time.LocalDate ngayCu,
            ScheduleItem moi,
            java.time.LocalDate ngayMoi) {
        Objects.requireNonNull(cu, "Công việc cũ không được null.");
        Objects.requireNonNull(moi, "Công việc mới không được null.");
        Objects.requireNonNull(ngayCu, "Ngày cũ không được null.");
        Objects.requireNonNull(ngayMoi, "Ngày mới không được null.");

        DaySchedule lichCu = layLich.apply(ngayCu);
        DaySchedule lichMoi = layLich.apply(ngayMoi);

        if (lichCu == null || lichMoi == null) {
            throw new IllegalStateException(
                    "Không tìm thấy lịch cần sửa.");
        }

        if (!lichCu.xoaItem(cu)) {
            throw new IllegalArgumentException(
                    "Không tìm thấy công việc cũ trong lịch.");
        }

        try {
            lichMoi.themItem(moi);
        } catch (RuntimeException ex) {
            // Khôi phục công việc cũ nếu thêm công việc mới thất bại.
            try {
                lichCu.themItem(cu);
            } catch (RuntimeException loiKhoiPhuc) {
                ex.addSuppressed(loiKhoiPhuc);
            }
            throw ex;
        }
    }

    public boolean xoaViec(
            ScheduleItem item,
            java.time.LocalDate ngay) {
        if (item == null || ngay == null) {
            return false;
        }

        DaySchedule lich = layLich.apply(ngay);
        return lich != null && lich.huyItem(item);
    }

    public void hoanThanh(ScheduleItem item) {
        Objects.requireNonNull(item, "Công việc không được null.");
        item.danhDauHoanThanh();
    }

    public void boHoanThanh(ScheduleItem item) {
        Objects.requireNonNull(item, "Công việc không được null.");
        item.boDanhDauHoanThanh();
    }
}