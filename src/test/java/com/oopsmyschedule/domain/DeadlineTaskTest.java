package com.oopsmyschedule.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

/** T-05 (INV-06, INV-21, K-05): hạn chót không được trước giờ kết thúc. */
class DeadlineTaskTest {

    private static final LocalDate NGAY = LocalDate.of(2026, 10, 16);

    private static DeadlineTask tao(LocalDateTime hanChot, LocalDate ngay) {
        return new DeadlineTask("Nộp bài", LocalTime.of(20, 0), LocalTime.of(21, 0),
                PriorityLevel.NEN_LAM, hanChot, ngay);
    }

    @Test
    void t05_hanChotTruocGioKetThuc_biTuChoi() {
        assertThrows(IllegalArgumentException.class,
                () -> tao(LocalDateTime.of(NGAY, LocalTime.of(20, 59)), NGAY));
    }

    @Test
    void t05_hanChotBangGioKetThuc_hopLe() {
        DeadlineTask t = tao(LocalDateTime.of(NGAY, LocalTime.of(21, 0)), NGAY);
        assertEquals(LocalDateTime.of(NGAY, LocalTime.of(21, 0)), t.getHanChot());
    }

    @Test
    void t05_hanChotSauGioKetThuc_hopLe() {
        assertDoesNotThrow(() -> tao(LocalDateTime.of(NGAY.plusDays(1), LocalTime.of(8, 0)), NGAY));
    }

    @Test
    void t05_hanChotLaNgayHomTruoc_biTuChoi() {
        assertThrows(IllegalArgumentException.class,
                () -> tao(LocalDateTime.of(NGAY.minusDays(1), LocalTime.of(23, 59)), NGAY));
    }

    @Test
    void inv21_ngayThucHienNull_biTuChoi() {
        assertThrows(IllegalArgumentException.class,
                () -> tao(LocalDateTime.of(NGAY, LocalTime.of(23, 59)), null));
    }

    @Test
    void inv06_constructorDocFile_khongKiem_deMoFileCu() {
        // 9 tham số = đường đọc từ file: cố ý không kiểm hạn chót để không làm hỏng file cũ.
        assertDoesNotThrow(() -> new DeadlineTask("id-1", "Cũ", LocalTime.of(20, 0), LocalTime.of(21, 0),
                PriorityLevel.NEN_LAM, false, LocalDateTime.of(NGAY, LocalTime.of(10, 0)), NGAY, false));
    }
}
