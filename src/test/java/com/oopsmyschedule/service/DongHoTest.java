package com.oopsmyschedule.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class DongHoTest {

    @Test
    void normalMode_gioKhongCoGiayVaNano() {
        LocalTime gio = new DongHo().layGioHienTai();
        assertEquals(0, gio.getSecond());
        assertEquals(0, gio.getNano());
    }

    @Test
    void demoMode_datGioCoGiayNano_traVeDungPhut() {
        DongHo dh = new DongHo();
        dh.datGioMoPhong(LocalTime.of(10, 15, 20, 123_456_789));
        LocalTime gio = dh.layGioHienTai();
        assertEquals(0, gio.getSecond());
        assertEquals(0, gio.getNano());
        assertTrue(gio.equals(LocalTime.of(10, 15)) || gio.equals(LocalTime.of(10, 16)));
    }

    @Test
    void demoMode_tuaNhanh30Phut_traVe1030() {
        DongHo dh = new DongHo();
        dh.datGioMoPhong(LocalTime.of(10, 0));
        dh.tuaNhanh(30);
        assertEquals(LocalTime.of(10, 30), dh.layGioHienTai());
    }
}
