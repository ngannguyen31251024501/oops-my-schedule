package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
class ReplanEngineTest {
    private static FixedEvent fixed(String ten, int gioBd, int phutBd, int gioKt, int phutKt) {
        return new FixedEvent(ten, LocalTime.of(gioBd, phutBd), LocalTime.of(gioKt, phutKt),
                PriorityLevel.BAT_BUOC);
    }

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

    @Test
    void engineTuChoiThamSoNull() {
        ReplanEngine e = new ReplanEngine();
        DaySchedule ds = new DaySchedule(LocalDate.now());
        Disruption d = new DelayDisruption(30);
        LocalTime t = LocalTime.of(10, 0);
        assertThrows(IllegalArgumentException.class, () -> e.taoCacPhuongAn(null, d, t));
        assertThrows(IllegalArgumentException.class, () -> e.taoCacPhuongAn(ds, null, t));
        assertThrows(IllegalArgumentException.class, () -> e.taoCacPhuongAn(ds, d, null));
    }
    @Test
    void t10_viecBatDauDungGioHienTai_biDoi() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        FlexibleTask v = new FlexibleTask("Viec A", LocalTime.of(10, 0), LocalTime.of(11, 0),
                PriorityLevel.CO_THE_BO);   // copy đúng cách tạo như test T-20 ở trên
        ds.themItem(v);

        List<PhuongAn> kq = new ReplanEngine()
                .taoCacPhuongAn(ds, new DelayDisruption(30), LocalTime.of(10, 0));

        for (PhuongAn p : kq) {
            ScheduleItem x = p.getKetQua().getLichSau().layLichHienTai().stream()
                    .filter(i -> i.getTen().equals("Viec A")).findFirst().orElse(null);
            // việc bắt đầu đúng 10:00 thuộc nhóm "còn lại" (không phải isBefore) nên phải bị dời hoặc đề xuất bỏ
            boolean biDoi = x != null && x.getGioBatDau().isAfter(LocalTime.of(10, 0));
            boolean biBo = p.getKetQua().getDanhSachDeXuatBo().stream()
                    .anyMatch(i -> i.getTen().equals("Viec A"));
            assertTrue(biDoi || biBo);
        }
    }
    @Disabled("Chờ nhóm chốt hành vi K-14: việc lỡ giờ")

    @Test
    void t11_viecBatDauTruocGioHienTai_khongBiDoi() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("Viec B", LocalTime.of(9, 0), LocalTime.of(9, 30),
                PriorityLevel.CO_THE_BO));

        List<PhuongAn> kq = new ReplanEngine()
                .taoCacPhuongAn(ds, new DelayDisruption(30), LocalTime.of(10, 0));

        for (PhuongAn p : kq) {
            ScheduleItem x = p.getKetQua().getLichSau().layLichHienTai().stream()
                    .filter(i -> i.getTen().equals("Viec B")).findFirst().orElseThrow();
            assertEquals(LocalTime.of(9, 0), x.getGioBatDau());   // đã qua, giữ nguyên
        }
    }
    @Test
    void t14_baChienLuocChoKetQuaKhacNhau() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("A", LocalTime.of(10, 0), LocalTime.of(11, 0), PriorityLevel.BAT_BUOC));
        ds.themItem(new FlexibleTask("B", LocalTime.of(11, 0), LocalTime.of(12, 30), PriorityLevel.CO_THE_BO));
        ds.themItem(new FlexibleTask("C", LocalTime.of(12, 30), LocalTime.of(14, 0), PriorityLevel.NEN_LAM));
        ds.themItem(fixed("Hop", 14, 0, 15, 0));

        List<PhuongAn> kq = new ReplanEngine()
                .taoCacPhuongAn(ds, new DelayDisruption(60), LocalTime.of(10, 0));

        assertEquals(3, kq.size());
        // so sánh "dấu vân tay" của từng phương án: danh sách giờ sau Replan + số việc đề xuất bỏ
        java.util.Set<String> dauVan = new java.util.HashSet<>();
        for (PhuongAn p : kq) {
            StringBuilder sb = new StringBuilder();
            p.getKetQua().getLichSau().layLichHienTai().forEach(i ->
                    sb.append(i.getTen()).append(i.getGioBatDau()).append(i.getGioKetThuc()).append(';'));
            sb.append("bo=").append(p.getKetQua().getDanhSachDeXuatBo().size());
            dauVan.add(sb.toString());
        }
        dauVan.forEach(System.out::println);
        assertTrue(dauVan.size() >= 2);
    }
    @Disabled("Chờ nhóm chốt hành vi K-14: việc lỡ giờ/dời dây chuyền")
    @Test
    void cascade_nhieuTang_khongChongKhongVuot2359() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("A", LocalTime.of(10, 0), LocalTime.of(11, 0), PriorityLevel.BAT_BUOC));
        ds.themItem(new FlexibleTask("B", LocalTime.of(11, 0), LocalTime.of(12, 0), PriorityLevel.BAT_BUOC));
        ds.themItem(new FlexibleTask("C", LocalTime.of(12, 0), LocalTime.of(13, 0), PriorityLevel.BAT_BUOC));
        ds.themItem(new FlexibleTask("D", LocalTime.of(13, 0), LocalTime.of(14, 0), PriorityLevel.BAT_BUOC));

        List<PhuongAn> kq = new ReplanEngine()
                .taoCacPhuongAn(ds, new DelayDisruption(30), LocalTime.of(9, 0));

        for (PhuongAn p : kq) {
            List<ScheduleItem> ls = new java.util.ArrayList<>(p.getKetQua().getLichSau().layLichHienTai());
            ls.sort((a, b) -> a.getGioBatDau().compareTo(b.getGioBatDau()));
            for (int i = 0; i < ls.size(); i++) {
                assertTrue(ls.get(i).getGioBatDau().isBefore(ls.get(i).getGioKetThuc()));
                if (i > 0) assertFalse(ls.get(i).getGioBatDau().isBefore(ls.get(i - 1).getGioKetThuc())); // không chồng
            }
            // dời theo dây chuyền: D phải bắt đầu sau 14:00 - 30' dời kéo từ A
            ScheduleItem d = ls.stream().filter(i -> i.getTen().equals("D")).findFirst().orElseThrow();
            assertEquals(LocalTime.of(13, 30), d.getGioBatDau());
        }
    }
}