package com.oopsmyschedule.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

/** DR-03b: T-05 (INV-06, K-05), INV-21, INV-08 (DR-01b). */
class DeadlineTaskGuardTest {

    private static final LocalDate NGAY = LocalDate.now();

    private static DeadlineTask tao(LocalTime ketThuc, LocalTime hanChot, LocalDate ngayThucHien) {
        return new DeadlineTask("d", LocalTime.of(9, 0), ketThuc, PriorityLevel.NEN_LAM,
                LocalDateTime.of(NGAY, hanChot), ngayThucHien);
    }

    // ---------- T-05: hạn chót không trước giờ kết thúc ----------

    @Test
    void t05_hanChotSauGioKetThuc_hopLe() {
        assertDoesNotThrow(() -> tao(LocalTime.of(10, 0), LocalTime.of(12, 0), NGAY));
    }

    @Test
    void t05_bien_hanChotBangGioKetThuc_hopLe() {
        assertDoesNotThrow(() -> tao(LocalTime.of(10, 0), LocalTime.of(10, 0), NGAY));
    }

    @Test
    void t05_bien_hanChotSomHon1Phut_biTuChoi() {
        assertThrows(IllegalArgumentException.class,
                () -> tao(LocalTime.of(10, 0), LocalTime.of(9, 59), NGAY));
    }

    @Test
    void t05_hanChotNull_biTuChoi() {
        assertThrows(IllegalArgumentException.class, () -> new DeadlineTask("d", LocalTime.of(9, 0),
                LocalTime.of(10, 0), PriorityLevel.NEN_LAM, null, NGAY));
    }

    // ---------- INV-21: ngayThucHien không null ----------

    @Test
    void inv21_ngayThucHienNull_biTuChoi() {
        assertThrows(IllegalArgumentException.class,
                () -> tao(LocalTime.of(10, 0), LocalTime.of(12, 0), null));
    }

    // ---------- INV-08: datLaiKhungGio giữ nguyên thời lượng ----------

    @Test
    void inv08_datLaiCungThoiLuong_hopLe() {
        FlexibleTask f = new FlexibleTask("f", LocalTime.of(9, 0), LocalTime.of(10, 0), PriorityLevel.NEN_LAM);
        f.ganLaiKhungGioBoiEngine(LocalTime.of(14, 0), LocalTime.of(15, 0));
        assertEquals(LocalTime.of(14, 0), f.getGioBatDau());
        assertEquals(LocalTime.of(15, 0), f.getGioKetThuc());
    }

    @Test
    void inv08_datLaiNganHon_hoacDaiHon_biTuChoi_vaGiuNguyenGio() {
        FlexibleTask f = new FlexibleTask("f", LocalTime.of(9, 0), LocalTime.of(10, 0), PriorityLevel.NEN_LAM);
        assertThrows(IllegalArgumentException.class,
                () -> f.ganLaiKhungGioBoiEngine(LocalTime.of(14, 0), LocalTime.of(14, 59))); // ngắn 1 phút
        assertThrows(IllegalArgumentException.class,
                () -> f.ganLaiKhungGioBoiEngine(LocalTime.of(14, 0), LocalTime.of(15, 1)));  // dài 1 phút
        assertEquals(LocalTime.of(9, 0), f.getGioBatDau());
        assertEquals(LocalTime.of(10, 0), f.getGioKetThuc());
    }
}