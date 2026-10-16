package com.oopsmyschedule.controller;

import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.FlexibleTask;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.ScheduleItem;
import com.oopsmyschedule.service.DaySchedule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplanControllerTest {

    private DaySchedule homNay() {
        return new DaySchedule(LocalDate.of(2026, 10, 15));
    }

    private ScheduleItem viec() {
        return new FlexibleTask("Đọc sách", LocalTime.of(14, 0), LocalTime.of(15, 0), PriorityLevel.CO_THE_BO);
    }

    @Test
    void viecChuaXepDuocGiuLaiOLichHomNay() {
        DaySchedule ds = homNay();
        ScheduleItem biBo = viec();
        ReplanController.giuLaiHomNay(ds, biBo);
        assertTrue(ds.layLichHienTai().stream().anyMatch(i -> i.getId().equals(biBo.getId())));
    }

    @Test
    void giuLaiHaiLanKhongBiTrung() {
        DaySchedule ds = homNay();
        ScheduleItem biBo = viec();
        ReplanController.giuLaiHomNay(ds, biBo);
        ReplanController.giuLaiHomNay(ds, biBo);
        assertEquals(1, ds.layLichHienTai().size());
    }

    @Test
    void viecTrungGioCoDinhKhongNemLoi() {
        DaySchedule ds = homNay();
        ds.themItem(new FixedEvent("Họp", LocalTime.of(14, 0), LocalTime.of(15, 0), PriorityLevel.BAT_BUOC));
        ScheduleItem biBo = new FlexibleTask("Đọc sách", LocalTime.of(14, 30), LocalTime.of(15, 30), PriorityLevel.CO_THE_BO);
        assertDoesNotThrow(() -> ReplanController.giuLaiHomNay(ds, biBo));
    }

    @Test
    void khongCoViecChuaXepThiLichKhongDoi() {
        DaySchedule ds = homNay();
        int truoc = ds.layLichHienTai().size();
        for (ScheduleItem bi : List.<ScheduleItem>of()) {
            ReplanController.giuLaiHomNay(ds, bi);
        }
        assertEquals(truoc, ds.layLichHienTai().size());
    }
}