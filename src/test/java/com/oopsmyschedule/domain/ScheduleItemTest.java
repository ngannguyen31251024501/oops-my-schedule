package com.oopsmyschedule.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** T-04 (INV-03,K-01/K-02): chặn dời việc qua 23:59.. */
class ScheduleItemTest {

    private static FlexibleTask fx(int h1, int m1, int h2, int m2) {
        return new FlexibleTask("v", LocalTime.of(h1, m1), LocalTime.of(h2, m2), PriorityLevel.NEN_LAM);
    }

    @Test
    void t04_vuotNuaDem_nem_va_giuNguyenGio() {
        FlexibleTask t = fx(23, 30, 23, 50);
        assertThrows(IllegalArgumentException.class, () -> t.xuLyKhiBiTre(20));
        assertEquals(LocalTime.of(23, 30), t.getGioBatDau());
        assertEquals(LocalTime.of(23, 50), t.getGioKetThuc());
        assertEquals(0, t.getSoLanDaDoi());
    }

    @Test
    void t04_bien_ketThucDung2359_hopLe() {
        FlexibleTask t = fx(23, 0, 23, 30);
        t.xuLyKhiBiTre(29);
        assertEquals(LocalTime.of(23, 59), t.getGioKetThuc());
    }

    @Test
    void t04_bien_vuot1Phut_biTuChoi() {
        assertThrows(IllegalArgumentException.class, () -> fx(23, 0, 23, 30).xuLyKhiBiTre(30));
    }

    @Test
    void t04_k01_23_00_den_23_59_tre5_biTuChoi() {
        assertThrows(IllegalArgumentException.class, () -> fx(23, 0, 23, 59).xuLyKhiBiTre(5));
    }

    @Test
    void vuotNuaDemNeuDoi_chiDoc_khongDoiTrangThai() {
        FlexibleTask t = fx(23, 0, 23, 30);
        assertTrue(t.vuotNuaDemNeuDoi(30));
        assertFalse(t.vuotNuaDemNeuDoi(29));
        assertFalse(t.vuotNuaDemNeuDoi(0));
        assertEquals(LocalTime.of(23, 30), t.getGioKetThuc());
    }

    @Test
    void delay_ghiViecVuotNgay_khongDoiViecDo_vaKhongCongDon() {
        FlexibleTask som = fx(10, 0, 11, 0);
        FlexibleTask muon = fx(23, 30, 23, 50);
        FixedEvent fixed = new FixedEvent("Họp", LocalTime.of(12, 0), LocalTime.of(13, 0), PriorityLevel.BAT_BUOC);
        List<ScheduleItem> ds = new ArrayList<>(List.of(som, muon, fixed));
        DelayDisruption d = new DelayDisruption(20);

        d.apDung(ds);
        assertEquals(LocalTime.of(10, 20), som.getGioBatDau());
        assertEquals(LocalTime.of(23, 30), muon.getGioBatDau());
        assertEquals(LocalTime.of(12, 0), fixed.getGioBatDau());
        assertEquals(List.of(muon), d.getViecVuotNgay());

        d.apDung(new ArrayList<>(List.of(muon)));
        assertEquals(1, d.getViecVuotNgay().size());
        assertThrows(UnsupportedOperationException.class, () -> d.getViecVuotNgay().add(som));
    }
}