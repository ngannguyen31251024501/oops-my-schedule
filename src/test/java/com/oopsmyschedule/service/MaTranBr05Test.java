package com.oopsmyschedule.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.oopsmyschedule.domain.DeadlineTask;
import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.FlexibleTask;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.ScheduleItem;

/** Ma trận BR-05: việc nào được phép đề xuất bỏ khi hết chỗ. */
class MaTranBr05Test {

    private final AbstractReplanStrategy itThayDoi = new MinimizeDisruptionStrategy();
    private final AbstractReplanStrategy uuTienDeadline = new DeadlineFirstStrategy();
    private final AbstractReplanStrategy giamTai = new ReduceWorkloadStrategy();

    private static ScheduleItem fixed() {
        return new FixedEvent("F", LocalTime.of(9, 0), LocalTime.of(10, 0), PriorityLevel.BAT_BUOC);
    }

    private static ScheduleItem flex(PriorityLevel p) {
        return new FlexibleTask("X", LocalTime.of(9, 0), LocalTime.of(10, 0), p);
    }

    private static ScheduleItem deadline(PriorityLevel p) {
        java.time.LocalDate ngay = java.time.LocalDate.now();
        return new DeadlineTask("D", LocalTime.of(9, 0), LocalTime.of(10, 0), p,
                ngay.plusDays(1).atTime(12, 0), ngay);
    }

    /** Thứ tự cột: Ít thay đổi, Ưu tiên deadline, Giảm tải. */
    private void kiem(ScheduleItem item, boolean a, boolean b, boolean c) {
        assertEquals(a, itThayDoi.coTheHySinh(item), "Ít thay đổi");
        assertEquals(b, uuTienDeadline.coTheHySinh(item), "Ưu tiên deadline");
        assertEquals(c, giamTai.coTheHySinh(item), "Giảm tải");
    }

    @Test void fixedEvent_khongBo()        { kiem(fixed(), false, false, false); }
    @Test void batBuoc_khongBo()           { kiem(flex(PriorityLevel.BAT_BUOC), false, false, false); }
    @Test void nenLam_khongDeadline()      { kiem(flex(PriorityLevel.NEN_LAM), false, true, true); }
    @Test void nenLam_deadline()           { kiem(deadline(PriorityLevel.NEN_LAM), false, false, true); }
    @Test void coTheBo_khongDeadline()     { kiem(flex(PriorityLevel.CO_THE_BO), true, true, true); }
    @Test void coTheBo_deadline()          { kiem(deadline(PriorityLevel.CO_THE_BO), true, false, true); }
}