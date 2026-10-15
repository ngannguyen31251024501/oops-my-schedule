package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** T-25, T-26, T-27 + lỗi giờ/mất việc/cảnh báo sai việc (K-13, K-14, T-28). */
class ReplanTreTest {

    private static LocalTime t(int h, int m) { return LocalTime.of(h, m); }

    private static DaySchedule lich0713() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("Ăn sáng", t(6, 20), t(6, 50), PriorityLevel.NEN_LAM));
        ds.themItem(new FixedEvent("Đi học", t(7, 10), t(11, 10), PriorityLevel.BAT_BUOC));
        ds.themItem(new FlexibleTask("Ăn trưa", t(11, 40), t(12, 10), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Làm báo cáo", t(13, 30), t(15, 0), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Ăn tối", t(18, 0), t(18, 30), PriorityLevel.NEN_LAM));
        return ds;
    }

    private static ScheduleItem tim(DaySchedule ds, String ten) {
        return ds.layLichHienTai().stream().filter(i -> i.getTen().equals(ten)).findFirst().orElse(null);
    }

    private static void khongChongGio(DaySchedule ds) {
        List<ScheduleItem> l = ds.layLichHienTai();
        for (int i = 0; i < l.size(); i++)
            for (int j = i + 1; j < l.size(); j++)
                assertFalse(l.get(i).trung(l.get(j)), l.get(i) + " chồng " + l.get(j));
    }

    @Test
    void t25_t26_baoTre0713_chiViecBiAnhHuongDoi_khongMatViec() {
        for (ReplanStrategy cl : List.of(new MinimizeDisruptionStrategy(), new DeadlineFirstStrategy(), new ReduceWorkloadStrategy())) {
            KetQuaSoSanh kq = cl.apDung(lich0713(), new DelayDisruption(53), t(7, 13));
            DaySchedule sau = kq.getLichSau();
            ScheduleItem hoc = tim(sau, "Đi học");
            assertEquals(t(7, 10), hoc.getGioBatDau());
            assertEquals(t(11, 10), hoc.getGioKetThuc());
            assertEquals(t(11, 40), tim(sau, "Ăn trưa").getGioBatDau());
            assertEquals(t(13, 30), tim(sau, "Làm báo cáo").getGioBatDau());
            assertEquals(t(18, 0), tim(sau, "Ăn tối").getGioBatDau(), "Ăn tối không được bị chuyển giờ");
            ScheduleItem sang = tim(sau, "Ăn sáng");
            assertNotNull(sang, "Việc lỡ giờ không được biến mất khỏi lịch");
            assertFalse(sang.getGioBatDau().isBefore(t(11, 10)), "Ăn sáng phải xếp sau giờ học");
            khongChongGio(sau);
            assertTrue(kq.getDanhSachThayDoi().stream().anyMatch(s -> s.contains("Ăn sáng") && s.contains("dời")));
            assertTrue(kq.getDanhSachThayDoi().stream().noneMatch(s -> s.contains("Ăn tối")), "Không báo thay đổi giả");
        }
    }

    @Test
    void t27_apDungHaiLan_khongTroiLich() {
        ReplanStrategy cl = new MinimizeDisruptionStrategy();
        DaySchedule lan1 = cl.apDung(lich0713(), new DelayDisruption(53), t(7, 13)).getLichSau();
        KetQuaSoSanh kq2 = cl.apDung(lan1, new DelayDisruption(53), t(7, 13));
        for (ScheduleItem a : lan1.layLichHienTai()) {
            ScheduleItem b = tim(kq2.getLichSau(), a.getTen());
            assertEquals(a.getGioBatDau(), b.getGioBatDau(), a.getTen());
            assertEquals(a.getGioKetThuc(), b.getGioKetThuc(), a.getTen());
        }
    }

    @Test
    void viecTuongLaiKhongXungDot_giuNguyenKhiBaoTre() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("Ăn sáng", t(8, 0), t(8, 30), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Học bài", t(14, 0), t(15, 0), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Ăn tối", t(18, 0), t(18, 30), PriorityLevel.NEN_LAM));
        KetQuaSoSanh kq = new MinimizeDisruptionStrategy().apDung(ds, new DelayDisruption(40), t(8, 40));
        assertEquals(t(14, 0), tim(kq.getLichSau(), "Học bài").getGioBatDau());
        assertEquals(t(18, 0), tim(kq.getLichSau(), "Ăn tối").getGioBatDau());
        assertEquals(t(8, 40), tim(kq.getLichSau(), "Ăn sáng").getGioBatDau());
    }

    @Test
    void khongCuonVongQuaNuaDem_khongNhayRaRangSang() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FlexibleTask("Học bài", t(22, 30), t(23, 30), PriorityLevel.NEN_LAM));
        KetQuaSoSanh kq = new MinimizeDisruptionStrategy().apDung(ds, new DelayDisruption(90), t(22, 30));
        ScheduleItem h = tim(kq.getLichSau(), "Học bài");
        assertNull(h, "Không đủ chỗ trước nửa đêm -> đề xuất sang ngày mai, không rơi vào 2–3h sáng");
        assertEquals(1, kq.getDanhSachDeXuatBo().size());
        // API mức item cũng không cuộn vòng
        FlexibleTask f = new FlexibleTask("X", t(23, 0), t(23, 30), PriorityLevel.NEN_LAM);
        new DelayDisruption(90).apDung(new java.util.ArrayList<>(List.of(f)));
        assertEquals(t(23, 0), f.getGioBatDau());
    }

    @Test
    void chuyenNgayMai_giuBuoiGoc_khongVeChieuHayRangSang() {
        DaySchedule mai = new DaySchedule(LocalDate.now().plusDays(1));
        mai.themItem(new FlexibleTask("Họp nhóm", t(15, 0), t(15, 30), PriorityLevel.NEN_LAM));
        mai.themItem(new FixedEvent("Học", t(8, 0), t(12, 0), PriorityLevel.BAT_BUOC));
        LocalTime tối = mai.timKhoangTrongGanGio(Duration.ofMinutes(30), t(18, 0));
        assertEquals(t(18, 0), tối);
        LocalTime sớm = mai.timKhoangTrongGanGio(Duration.ofMinutes(30), t(1, 0));
        assertTrue(!sớm.isBefore(t(6, 0)));
    }

    @Test
    void canhBao_khongNhacLopDangHoc_vaChonViecTreLauNhat() {
        DaySchedule ds = lich0713();
        List<CanhBaoViec> ds0713 = BoTheoDoiLich.quetChoNguoiDung(ds, t(7, 13));
        assertTrue(ds0713.stream().noneMatch(c -> c.getItem().getTen().equals("Đi học")));
        assertEquals("Ăn sáng", BoTheoDoiLich.chonCanhBaoChinh(ds0713).getItem().getTen());
        // sau khi áp dụng phương án, bữa sáng đã được xếp lại -> không còn cảnh báo cho nó
        DaySchedule sau = new MinimizeDisruptionStrategy().apDung(ds, new DelayDisruption(53), t(7, 13)).getLichSau();
        assertTrue(BoTheoDoiLich.quetChoNguoiDung(sau, t(7, 13)).isEmpty());
    }
}
