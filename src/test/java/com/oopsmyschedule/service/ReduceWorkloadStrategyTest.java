package com.oopsmyschedule.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.FlexibleTask;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.ScheduleItem;

/** T-21 (K-10): Giảm tải không cắt việc khi chưa quá tải. */
class ReduceWorkloadStrategyTest {

    private static FlexibleTask viec(String ten, int gioBd, int gioKt) {
        return new FlexibleTask(ten, LocalTime.of(gioBd, 0), LocalTime.of(gioKt, 0),
                PriorityLevel.CO_THE_BO);
    }

    @Test
    void t21_fixedKhongBiDemDoi_chuaQuaTai_khongCatViecNao() {
        // Fixed 14-16 + 5 việc "Có thể bỏ" x 120', bây giờ 10:00
        // Cần 600' <= có sẵn 719' => không được cắt việc nào.
        List<ScheduleItem> conLai = new ArrayList<>();
        conLai.add(new FixedEvent("Hoc", LocalTime.of(14, 0), LocalTime.of(16, 0),
                PriorityLevel.BAT_BUOC));
        conLai.add(viec("A", 10, 12));
        conLai.add(viec("B", 12, 14));
        conLai.add(viec("C", 16, 18));
        conLai.add(viec("D", 18, 20));
        conLai.add(viec("E", 20, 22));

        KetQuaSoSanh ketQua = new KetQuaSoSanh(
                new DaySchedule(LocalDate.of(2026, 10, 12)),
                new DaySchedule(LocalDate.of(2026, 10, 12)));

        List<ScheduleItem> kq = new ReduceWorkloadStrategy()
                .tienXuLyTruocKhiQuet(conLai, LocalTime.of(10, 0), ketQua);

        assertEquals(6, kq.size());
        assertTrue(ketQua.getDanhSachDeXuatBo().isEmpty());
    }
}
