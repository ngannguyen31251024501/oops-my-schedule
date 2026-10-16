package com.oopsmyschedule.controller;

import com.oopsmyschedule.domain.*;
import com.oopsmyschedule.service.DaySchedule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduleControllerTest {
    private final DaySchedule ds = new DaySchedule(LocalDate.of(2026, 10, 15));
    private final ScheduleController c = new ScheduleController(d -> ds);

    @Test
    void themViecNullBiTuChoi() {
        assertThrows(IllegalArgumentException.class, () -> c.themViec(null, ds.getNgay()));
    }

    @Test
    void themViecNgayNullBiTuChoi() {
        assertThrows(IllegalArgumentException.class, () ->
                c.themViec(new FixedEvent("Học", LocalTime.of(9, 0), LocalTime.of(10, 0), PriorityLevel.BAT_BUOC), null));
    }

    @Test
    void themFixedTrungGioBiChan() {
        c.themViec(new FixedEvent("Học", LocalTime.of(9, 0), LocalTime.of(10, 0), PriorityLevel.BAT_BUOC), ds.getNgay());
        assertThrows(IllegalArgumentException.class, () ->
                c.themViec(new FixedEvent("Họp", LocalTime.of(9, 30), LocalTime.of(10, 30), PriorityLevel.BAT_BUOC), ds.getNgay()));
    }

    @Test
    void xoaViecNullTraVeFalse() {
        assertEquals(false, c.xoaViec(null, ds.getNgay()));
    }
}