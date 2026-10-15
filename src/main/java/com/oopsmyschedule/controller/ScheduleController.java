package com.oopsmyschedule.controller;

import com.oopsmyschedule.domain.ScheduleItem;
import com.oopsmyschedule.service.DaySchedule;

import java.time.LocalDate;
import java.util.function.Function;

/**
 * Điều phối các thao tác thêm, sửa, xóa và hoàn thành công việc.
 * Controller không trực tiếp phụ thuộc vào MainFrame.
 * Input null bị từ chối bằng IllegalArgumentException; luật nghiệp vụ do Domain kiểm tra.
 */
public class ScheduleController {

    private final Function<LocalDate, DaySchedule> layLich;

    public ScheduleController(Function<LocalDate, DaySchedule> layLich) {
        if (layLich == null) {
            throw new IllegalArgumentException("Hàm lấy lịch không được null.");
        }
        this.layLich = layLich;
    }

    public void themViec(ScheduleItem item, LocalDate ngay) {
        if (item == null) {
            throw new IllegalArgumentException("Công việc không được null.");
        }
        if (ngay == null) {
            throw new IllegalArgumentException("Ngày không được null.");
        }
        DaySchedule lich = layLich.apply(ngay);
        if (lich == null) {
            throw new IllegalStateException("Không tìm thấy lịch của ngày đã chọn.");
        }
        lich.themItem(item);
    }

    public void suaViec(ScheduleItem cu, LocalDate ngayCu, ScheduleItem moi, LocalDate ngayMoi) {
        if (cu == null || moi == null) {
            throw new IllegalArgumentException("Công việc cũ và mới không được null.");
        }
        if (ngayCu == null || ngayMoi == null) {
            throw new IllegalArgumentException("Ngày cũ và ngày mới không được null.");
        }

        DaySchedule lichCu = layLich.apply(ngayCu);
        DaySchedule lichMoi = layLich.apply(ngayMoi);
        if (lichCu == null || lichMoi == null) {
            throw new IllegalStateException("Không tìm thấy lịch cần sửa.");
        }

        if (!lichCu.xoaItem(cu)) {
            throw new IllegalArgumentException("Không tìm thấy công việc cũ trong lịch.");
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

    public boolean xoaViec(ScheduleItem item, LocalDate ngay) {
        if (item == null || ngay == null) {
            return false;
        }
        DaySchedule lich = layLich.apply(ngay);
        return lich != null && lich.huyItem(item);
    }

    public void hoanThanh(ScheduleItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Công việc không được null.");
        }
        item.danhDauHoanThanh();
    }

    public void boHoanThanh(ScheduleItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Công việc không được null.");
        }
        item.boDanhDauHoanThanh();
    }
}