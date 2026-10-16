package com.oopsmyschedule.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**DR-03: JUnit Domain, mỗi lớp đủ ca đúng / ca sai / ca biên + đa hình.*/
class DomainBoundaryTest {

    private static LocalTime t(int h, int m) {
        return LocalTime.of(h, m);
    }

    private static FlexibleTask flex(int h1, int m1, int h2, int m2) {
        return new FlexibleTask("v", t(h1, m1), t(h2, m2), PriorityLevel.NEN_LAM);
    }

    private static DeadlineTask deadline(int h1, int m1, int h2, int m2, int hanH, int hanM) {
        return new DeadlineTask("d", t(h1, m1), t(h2, m2), PriorityLevel.NEN_LAM,
                LocalDateTime.of(LocalDate.now(), t(hanH, hanM)), LocalDate.now());
    }

    // ---------- ScheduleItem (qua FlexibleTask): INV-01, 02 ----------

    @Test
    void item_hopLe_nhanVaTinhThoiLuong() {
        FlexibleTask f = flex(10, 0, 11, 0);
        assertEquals(Duration.ofMinutes(60), f.layThoiLuong());
        assertEquals("v", f.getTen());
    }

    @Test
    void item_ten_nullRongKhoangTrang_biTuChoi() {
        assertThrows(IllegalArgumentException.class, () -> new FlexibleTask(null, t(10, 0), t(11, 0), PriorityLevel.NEN_LAM));
        assertThrows(IllegalArgumentException.class, () -> new FlexibleTask("", t(10, 0), t(11, 0), PriorityLevel.NEN_LAM));
        assertThrows(IllegalArgumentException.class, () -> new FlexibleTask("   ", t(10, 0), t(11, 0), PriorityLevel.NEN_LAM));
    }

    @Test
    void item_ten_motKyTu_hopLe() {
        assertDoesNotThrow(() -> new FlexibleTask("a", t(10, 0), t(11, 0), PriorityLevel.NEN_LAM));
    }

    @Test
    void item_gio_null_biTuChoi() {
        assertThrows(IllegalArgumentException.class, () -> new FlexibleTask("a", null, t(11, 0), PriorityLevel.NEN_LAM));
        assertThrows(IllegalArgumentException.class, () -> new FlexibleTask("a", t(10, 0), null, PriorityLevel.NEN_LAM));
    }

    @Test
    void item_gio_bien() {
        assertDoesNotThrow(() -> flex(10, 0, 10, 1));                      // 1 phút hợp lệ
        assertThrows(IllegalArgumentException.class, () -> flex(10, 0, 10, 0)); // bằng nhau bị từ chối
        assertThrows(IllegalArgumentException.class, () -> flex(11, 0, 10, 0)); // đảo ngược
        assertDoesNotThrow(() -> flex(23, 0, 23, 59));                     // kết thúc đúng 23:59
    }

    @Test
    void item_trungGio_bien() {
        FlexibleTask a = flex(10, 0, 11, 0);
        assertFalse(a.trung(flex(11, 0, 12, 0)), "nối tiếp không phải chồng giờ");
        assertTrue(a.trung(flex(10, 59, 12, 0)), "chồng 1 phút");
        assertFalse(a.trung(flex(8, 0, 10, 0)));
    }

    @Test
    void item_equals_theoId_khongTheoNoiDung() {
        FlexibleTask a = flex(10, 0, 11, 0);
        FlexibleTask cungNoiDung = flex(10, 0, 11, 0);
        FlexibleTask cungId = new FlexibleTask(a.getId(), "khac", t(15, 0), t(16, 0),
                PriorityLevel.CO_THE_BO, false, 0);
        assertNotEquals(a, cungNoiDung);
        assertEquals(a, cungId);
        assertEquals(a.hashCode(), cungId.hashCode());
    }

    // ---------- FixedEvent ----------

    @Test
    void fixed_khongDoiDuoc() {
        FixedEvent f = new FixedEvent("Họp", t(9, 0), t(10, 0), PriorityLevel.BAT_BUOC);
        assertFalse(f.coTheDoiLich());
        assertThrows(IllegalStateException.class, () -> f.xuLyKhiBiTre(10));
        assertEquals(t(9, 0), f.getGioBatDau());
    }

    // ---------- FlexibleTask ----------

    @Test
    void flexible_doiGio_vaTangSoLanDaDoi() {
        FlexibleTask f = flex(10, 0, 11, 0);
        f.xuLyKhiBiTre(90);
        assertEquals(t(11, 30), f.getGioBatDau());
        assertEquals(t(12, 30), f.getGioKetThuc());
        assertEquals(1, f.getSoLanDaDoi());
        assertEquals(Duration.ofMinutes(60), f.layThoiLuong(), "không nén thời lượng");
    }

    // ---------- DeadlineTask: BR-07 (biên đúng hạn / +1 phút) ----------

    @Test
    void deadline_ketThucDungHan_vanKip_coDoi_coTat() {
        DeadlineTask d = deadline(20, 0, 21, 0, 21, 30);
        d.xuLyKhiBiTre(30);
        assertEquals(t(20, 30), d.getGioBatDau());
        assertEquals(t(21, 30), d.getGioKetThuc());
        assertFalse(d.isCanhBaoTreHan());
    }

    @Test
    void deadline_troi1Phut_khongDoi_batCo() {
        DeadlineTask d = deadline(20, 0, 21, 0, 21, 30);
        d.xuLyKhiBiTre(31);
        assertEquals(t(20, 0), d.getGioBatDau());
        assertEquals(t(21, 0), d.getGioKetThuc());
        assertTrue(d.isCanhBaoTreHan());
    }

    @Test
    void deadline_hanChotNull_biTuChoi() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeadlineTask("d", t(9, 0), t(10, 0), PriorityLevel.NEN_LAM, null));
    }

    @Test
    void deadline_dongBoCo_theoGioKetThucHienTai() {
        DeadlineTask d = deadline(20, 0, 21, 0, 21, 30);
        d.ganLaiKhungGioBoiEngine(t(21, 0), t(22, 0)); // quá hạn do engine ép
        d.dongBoCanhBaoTheoKhungGioHienTai();
        assertTrue(d.isCanhBaoTreHan());
        d.ganLaiKhungGioBoiEngine(t(20, 0), t(21, 0));
        d.dongBoCanhBaoTheoKhungGioHienTai();
        assertFalse(d.isCanhBaoTreHan());
    }

    // ---------- RecurringEvent ----------

    @Test
    void recurring_boQuaHomNay_resetStreak_khongBienMat() {
        RecurringEvent r = new RecurringEvent("Chạy", t(6, 0), t(7, 0), PriorityLevel.NEN_LAM, 5);
        r.xuLyKhiBiTre(30);
        assertEquals(t(6, 30), r.getGioBatDau());
        assertEquals(5, r.getSoNgayLienTiep(), "dời thường không reset streak");
        r.boQuaHomNay();
        assertTrue(r.isBoQuaHomNay());
        assertEquals(0, r.getSoNgayLienTiep());
    }

    // ---------- Disruption ----------

    @Test
    void delay_bienPhutTre() {
        assertThrows(IllegalArgumentException.class, () -> new DelayDisruption(0));
        assertThrows(IllegalArgumentException.class, () -> new DelayDisruption(-1));
        assertDoesNotThrow(() -> new DelayDisruption(1));
    }

    @Test
    void skip_boViec_khoiDanhSach_vaNullBiTuChoi() {
        FlexibleTask a = flex(10, 0, 11, 0);
        FlexibleTask b = flex(12, 0, 13, 0);
        List<ScheduleItem> ds = new ArrayList<>(List.of(a, b));
        new SkipDisruption(a).apDung(ds);
        assertEquals(List.of(b), ds);
        assertThrows(RuntimeException.class, () -> new SkipDisruption(null)); // K-09: hiện là NPE
    }

    @Test
    void skip_recurring_khongXoa_chiBoQuaHomNay() {
        RecurringEvent r = new RecurringEvent("Chạy", t(6, 0), t(7, 0), PriorityLevel.NEN_LAM, 2);
        List<ScheduleItem> ds = new ArrayList<>(List.of(r));
        new SkipDisruption(r).apDung(ds);
        assertEquals(1, ds.size());
        assertTrue(r.isBoQuaHomNay());
    }

    // ---------- Đa hình: cùng gọi xuLyKhiBiTre trên 4 loại -> 4 kết quả khác nhau ----------

    @Test
    void daHinh_xuLyKhiBiTre_4Loai_4KetQua() {
        FixedEvent fx = new FixedEvent("a", t(8, 0), t(9, 0), PriorityLevel.BAT_BUOC);
        FlexibleTask fl = flex(9, 0, 10, 0);
        RecurringEvent re = new RecurringEvent("c", t(11, 0), t(12, 0), PriorityLevel.NEN_LAM, 1);
        DeadlineTask dl = deadline(13, 0, 14, 0, 14, 10);

        assertThrows(IllegalStateException.class, () -> fx.xuLyKhiBiTre(30)); // 1: ném lỗi
        fl.xuLyKhiBiTre(30);
        assertEquals(t(9, 30), fl.getGioBatDau());                            // 2: dời + đếm
        re.xuLyKhiBiTre(30);
        assertEquals(t(11, 30), re.getGioBatDau());                           // 3: dời, không đếm
        dl.xuLyKhiBiTre(30);
        assertEquals(t(13, 0), dl.getGioBatDau());                            // 4: không dời, bật cờ
        assertTrue(dl.isCanhBaoTreHan());
    }

    @Test
    void daHinh_delay_apDungTrenDanhSachHonHop() {
        FixedEvent fx = new FixedEvent("f", t(12, 0), t(13, 0), PriorityLevel.BAT_BUOC);
        FlexibleTask fl = flex(10, 0, 11, 0);
        List<ScheduleItem> ds = new ArrayList<>(List.of(fl, fx));
        new DelayDisruption(15).apDung(ds);
        assertEquals(t(10, 15), fl.getGioBatDau());
        assertEquals(t(12, 0), fx.getGioBatDau());
    }
}