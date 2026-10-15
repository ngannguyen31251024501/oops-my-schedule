package com.oopsmyschedule.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** [NG-02a] Domain: chuyển từ KiemThu.java (TC-01..08 phần domain + BONUS-3). */
class ScheduleItemTest {

    private static LocalTime t(int h, int m) {
        return LocalTime.of(h, m);
    }

    @Test
    void tc02_flexibleTaskDoiDungSlot() {
        FlexibleTask x = new FlexibleTask("Làm bài", t(10, 0), t(11, 0), PriorityLevel.NEN_LAM);
        new DelayDisruption(90).apDung(new ArrayList<>(List.of(x)));
        assertEquals(t(11, 30), x.getGioBatDau());
        assertEquals(t(12, 30), x.getGioKetThuc());
        assertEquals(1, x.getSoLanDaDoi());
    }

    @Test
    void tc03_fixedEventKhongBiDoi() {
        FixedEvent e = new FixedEvent("Họp nhóm", t(14, 0), t(15, 0), PriorityLevel.BAT_BUOC);
        new DelayDisruption(90).apDung(new ArrayList<>(List.of(e)));
        assertEquals(t(14, 0), e.getGioBatDau());
        assertEquals(t(15, 0), e.getGioKetThuc());
    }

    @Test
    void tc04_fixedEventGoiTrucTiepNemIllegalState() {
        FixedEvent e = new FixedEvent("Họp nhóm", t(14, 0), t(15, 0), PriorityLevel.BAT_BUOC);
        assertThrows(IllegalStateException.class, () -> e.xuLyKhiBiTre(30));
        assertFalse(e.coTheDoiLich());
    }

    @Test
    void tc05_deadlineConKipHan() {
        LocalDate homNay = LocalDate.now();
        DeadlineTask d = new DeadlineTask("Nộp báo cáo", t(20, 0), t(21, 0), PriorityLevel.NEN_LAM,
                LocalDateTime.of(homNay.plusDays(1), t(23, 59)), homNay);
        d.xuLyKhiBiTre(60);
        assertFalse(d.isCanhBaoTreHan());
        assertEquals(t(21, 0), d.getGioBatDau());
    }

    @Test
    void tc06_deadlineKhongKipHanThiKhongDoiVaBatCo() {
        LocalDate homNay = LocalDate.now();
        DeadlineTask d = new DeadlineTask("Nộp báo cáo", t(21, 30), t(22, 0), PriorityLevel.NEN_LAM,
                LocalDateTime.of(homNay, t(23, 59)), homNay);
        d.xuLyKhiBiTre(150);
        assertTrue(d.isCanhBaoTreHan());
        assertEquals(t(21, 30), d.getGioBatDau());
    }

    @Test
    void t10_deadlineBienKetThucDungHanVaCongMotPhut() {
        LocalDate homNay = LocalDate.now();
        // hạn 21:30, việc 20:00-21:00, trễ 30 -> kết thúc 21:30 = hạn -> vẫn kịp
        DeadlineTask a = new DeadlineTask("A", t(20, 0), t(21, 0), PriorityLevel.NEN_LAM,
                LocalDateTime.of(homNay, t(21, 30)), homNay);
        a.xuLyKhiBiTre(30);
        assertFalse(a.isCanhBaoTreHan());
        assertEquals(t(20, 30), a.getGioBatDau());
        // trễ 31 -> quá hạn 1 phút -> không dời, bật cờ
        DeadlineTask b = new DeadlineTask("B", t(20, 0), t(21, 0), PriorityLevel.NEN_LAM,
                LocalDateTime.of(homNay, t(21, 30)), homNay);
        b.xuLyKhiBiTre(31);
        assertTrue(b.isCanhBaoTreHan());
        assertEquals(t(20, 0), b.getGioBatDau());
    }

    @Test
    void tc07_recurringBoQuaHomNayResetStreak() {
        RecurringEvent gym = new RecurringEvent("Gym", t(18, 0), t(19, 0), PriorityLevel.NEN_LAM, 5);
        gym.boQuaHomNay();
        assertEquals(0, gym.getSoNgayLienTiep());
        assertTrue(gym.isBoQuaHomNay());
    }

    @Test
    void tc08_skipGiaiPhongKhungGio() {
        FlexibleTask x = new FlexibleTask("Đọc tài liệu", t(15, 0), t(16, 0), PriorityLevel.CO_THE_BO);
        List<ScheduleItem> ds = new ArrayList<>(List.of(x));
        new SkipDisruption(x).apDung(ds);
        assertTrue(ds.isEmpty());
    }

    @Test
    void bonus3_gioBatDauKhongTruocGioKetThucBiTuChoi() {
        assertThrows(IllegalArgumentException.class,
                () -> new FlexibleTask("X", t(11, 0), t(10, 0), PriorityLevel.NEN_LAM));
        assertThrows(IllegalArgumentException.class,
                () -> new FlexibleTask("X", t(10, 0), t(10, 0), PriorityLevel.NEN_LAM));
    }

    @Test
    void t01_tenRongHoacNullBiTuChoi() {
        assertThrows(IllegalArgumentException.class, () -> new FlexibleTask("", t(9, 0), t(10, 0), PriorityLevel.NEN_LAM));
        assertThrows(IllegalArgumentException.class, () -> new FlexibleTask("   ", t(9, 0), t(10, 0), PriorityLevel.NEN_LAM));
        assertThrows(IllegalArgumentException.class, () -> new FlexibleTask(null, t(9, 0), t(10, 0), PriorityLevel.NEN_LAM));
        assertDoesNotThrow(() -> new FlexibleTask("A", t(9, 0), t(9, 1), PriorityLevel.NEN_LAM)); // biên: 1 ký tự, 1 phút
    }

    @Test
    void t07_delayPhutKhongHopLe() {
        assertThrows(IllegalArgumentException.class, () -> new DelayDisruption(0));
        assertThrows(IllegalArgumentException.class, () -> new DelayDisruption(-1));
        assertDoesNotThrow(() -> new DelayDisruption(1));
    }

    @Test
    void daHinh_cungMotLoiGoiBonKetQua() {
        LocalDate hn = LocalDate.now();
        FlexibleTask f = new FlexibleTask("F", t(10, 0), t(11, 0), PriorityLevel.NEN_LAM);
        RecurringEvent r = new RecurringEvent("R", t(10, 0), t(11, 0), PriorityLevel.NEN_LAM, 2);
        DeadlineTask d = new DeadlineTask("D", t(10, 0), t(11, 0), PriorityLevel.NEN_LAM,
                LocalDateTime.of(hn.plusDays(1), t(23, 59)), hn);
        FixedEvent x = new FixedEvent("X", t(10, 0), t(11, 0), PriorityLevel.BAT_BUOC);
        for (ScheduleItem i : List.of(f, r, d)) {
            i.xuLyKhiBiTre(30);
            assertEquals(t(10, 30), i.getGioBatDau(), i.loaiViec());
        }
        assertThrows(IllegalStateException.class, () -> x.xuLyKhiBiTre(30));
    }
}
