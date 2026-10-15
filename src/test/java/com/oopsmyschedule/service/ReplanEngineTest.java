package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReplanEngineTest {

    @Test
    void t20_viecDaHoanThanhKhongBiDoi() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        FlexibleTask xong = new FlexibleTask("Da xong", LocalTime.of(14, 0), LocalTime.of(15, 0),
                PriorityLevel.CO_THE_BO);
        xong.danhDauHoanThanh();
        FlexibleTask dangLam = new FlexibleTask("Dang lam", LocalTime.of(13, 0), LocalTime.of(14, 20),
                PriorityLevel.CO_THE_BO);
        ds.themItem(xong);
        ds.themItem(dangLam);

        List<PhuongAn> kq = new ReplanEngine()
                .taoCacPhuongAn(ds, new DelayDisruption(90), LocalTime.of(12, 0)); // SỬA nếu constructor khác

        for (PhuongAn p : kq) {
            ScheduleItem x = p.getKetQua().getLichSau().layLichHienTai().stream()
                    .filter(i -> i.getTen().equals("Da xong")).findFirst().orElseThrow();
            assertEquals(LocalTime.of(14, 0), x.getGioBatDau());
            assertEquals(LocalTime.of(15, 0), x.getGioKetThuc());
            assertFalse(p.getKetQua().getDanhSachDeXuatBo().contains(x));
        }
    }
}