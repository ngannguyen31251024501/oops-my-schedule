package com.oopsmyschedule.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.oopsmyschedule.domain.DelayDisruption;
import com.oopsmyschedule.domain.FlexibleTask;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.ScheduleItem;

/** T-04 (K-01/K-02): dời qua nửa đêm không được sinh giờ sai (vd. 23:50-00:10). */
class NuaDemTest {

    @Test
    void t04_viec2330_tre20Phut_khongSinhGioQuanVong() {
        DaySchedule ds = new DaySchedule(LocalDate.of(2026, 10, 12));
        FlexibleTask viec = new FlexibleTask("Viec muon", LocalTime.of(23, 30),
                LocalTime.of(23, 50), PriorityLevel.NEN_LAM);
        ds.themItem(viec);

        // đang 23:40, trễ 20 phút: 23:50 + 20 phút sẽ vượt 23:59
        KetQuaSoSanh kq = new MinimizeDisruptionStrategy()
                .apDung(ds, new DelayDisruption(20), LocalTime.of(23, 40));

        // 1) không item nào có giờ kết thúc nằm trước giờ bắt đầu (lỗi quấn vòng 00:00)
        for (ScheduleItem it : kq.getLichSau().layLichHienTai()) {
            assertFalse(it.getGioKetThuc().isBefore(it.getGioBatDau()),
                    it.getTen() + " bị quấn vòng qua 00:00");
        }
        // 2) không mất việc: còn trong lịch hoặc nằm trong danh sách đề xuất bỏ
        boolean conTrongLich = kq.getLichSau().layLichHienTai().stream()
                .anyMatch(i -> i.getTen().equals("Viec muon"));
        boolean deXuatBo = kq.getDanhSachDeXuatBo().stream()
                .anyMatch(i -> i.getTen().equals("Viec muon"));
        assertTrue(conTrongLich || deXuatBo, "Việc không được biến mất");
    }

    @Test
    void t04_kheBienSat2359_viecKhongVuotNgay() {
        DaySchedule ds = new DaySchedule(LocalDate.of(2026, 10, 12));
        ds.themItem(new FlexibleTask("A", LocalTime.of(22, 0), LocalTime.of(23, 0),
                PriorityLevel.NEN_LAM));

        KetQuaSoSanh kq = new MinimizeDisruptionStrategy()
                .apDung(ds, new DelayDisruption(60), LocalTime.of(21, 0));

        for (ScheduleItem it : kq.getLichSau().layLichHienTai()) {
            assertFalse(it.getGioKetThuc().isAfter(LocalTime.of(23, 59)));
            assertFalse(it.getGioKetThuc().isBefore(it.getGioBatDau()));
        }
    }
}