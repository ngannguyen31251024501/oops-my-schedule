package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.DeadlineTask;
import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.FlexibleTask;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.RecurringEvent;
import com.oopsmyschedule.domain.ScheduleItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** [NG-03a / NG-03b] BoTheoDoiLich: phân loại mức trễ + overload idBoQua (K-04, Q-04) + không tự Replan (BR-03). */
class BoTheoDoiLichTest {

    private static DaySchedule lichMotViec(int gio, int phut, ScheduleItem[] ra) {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ScheduleItem i = new FlexibleTask("Việc", LocalTime.of(gio, phut), LocalTime.of(gio + 1, phut), PriorityLevel.NEN_LAM);
        ds.themItem(i);
        ra[0] = i;
        return ds;
    }

    private static MucDoCanhBao mucTai(int phutSauBatDau) {
        ScheduleItem[] r = new ScheduleItem[1];
        DaySchedule ds = lichMotViec(13, 0, r);
        List<CanhBaoViec> kq = BoTheoDoiLich.quet(ds, LocalTime.of(13, 0).plusMinutes(phutSauBatDau));
        assertEquals(1, kq.size());
        return kq.get(0).getMucDo();
    }

    @Test
    void t22_bienMucTre_0_4_5_19_20() {
        assertEquals(MucDoCanhBao.MOI_BAT_DAU, mucTai(0));
        assertEquals(MucDoCanhBao.MOI_BAT_DAU, mucTai(4));
        assertEquals(MucDoCanhBao.TRE_NHE, mucTai(5));
        assertEquals(MucDoCanhBao.TRE_NHE, mucTai(19));
        assertEquals(MucDoCanhBao.TRE_NANG, mucTai(20));
    }

    @Test
    void chuaToiGioThiKhongCanhBao() {
        ScheduleItem[] r = new ScheduleItem[1];
        DaySchedule ds = lichMotViec(13, 0, r);
        assertTrue(BoTheoDoiLich.quet(ds, LocalTime.of(12, 59)).isEmpty());
    }

    @Test
    void viecDaHoanThanhKhongCanhBao() {
        ScheduleItem[] r = new ScheduleItem[1];
        DaySchedule ds = lichMotViec(6, 0, r);
        r[0].danhDauHoanThanh();
        assertTrue(BoTheoDoiLich.quet(ds, LocalTime.of(8, 0)).isEmpty());
    }

    @Test
    void idBoQua_viecBiBoQuaKhongConCanhBao_viecKhacVanCanhBao() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ScheduleItem a = new FlexibleTask("A", LocalTime.of(9, 0), LocalTime.of(10, 0), PriorityLevel.NEN_LAM);
        ScheduleItem b = new FlexibleTask("B", LocalTime.of(9, 30), LocalTime.of(10, 30), PriorityLevel.NEN_LAM);
        ds.themItem(a);
        ds.themItem(b);
        List<CanhBaoViec> kq = BoTheoDoiLich.quet(ds, LocalTime.of(10, 0), Set.of(a.getId()));
        assertEquals(1, kq.size());
        assertSame(b, kq.get(0).getItem());
    }

    @Test
    void idBoQua_khongDanhDauHoanThanhVaKhongXoaViec() {
        ScheduleItem[] r = new ScheduleItem[1];
        DaySchedule ds = lichMotViec(9, 0, r);
        BoTheoDoiLich.quet(ds, LocalTime.of(10, 0), Set.of(r[0].getId()));
        assertFalse(r[0].isHoanThanh());
        assertEquals(1, ds.layLichHienTai().size());
    }

    @Test
    void overloadRongGiongBanCu() {
        ScheduleItem[] r = new ScheduleItem[1];
        DaySchedule ds = lichMotViec(9, 0, r);
        assertEquals(BoTheoDoiLich.quet(ds, LocalTime.of(9, 30)).size(),
                BoTheoDoiLich.quet(ds, LocalTime.of(9, 30), Set.of()).size());
    }

    @Test
    void thamSoNullBiTuChoi() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        assertThrows(IllegalArgumentException.class, () -> BoTheoDoiLich.quet(ds, LocalTime.NOON, null));
        assertThrows(IllegalArgumentException.class, () -> BoTheoDoiLich.quet(null, LocalTime.NOON, Set.of()));
    }

    // ---------- BR-03: chỉ báo hiệu, KHÔNG tự Replan / không đổi lịch ----------

    /** Chụp toàn bộ trạng thái lịch (giờ, cờ, bộ đếm) thành chuỗi để so sánh trước/sau khi quét. */
    private static String chupLich(DaySchedule ds) {
        StringBuilder sb = new StringBuilder();
        for (ScheduleItem i : ds.layLichHienTai()) {
            sb.append(i.getId()).append('|').append(i.loaiViec()).append('|')
              .append(i.getGioBatDau()).append('-').append(i.getGioKetThuc()).append('|')
              .append(i.isHoanThanh()).append('|').append(i.moTaTrangThai());
            if (i instanceof FlexibleTask f) sb.append("|doi=").append(f.getSoLanDaDoi());
            if (i instanceof DeadlineTask d) sb.append("|treHan=").append(d.isCanhBaoTreHan());
            if (i instanceof RecurringEvent r) sb.append("|boQua=").append(r.isBoQuaHomNay()).append("|streak=").append(r.getSoNgayLienTiep());
            sb.append('\n');
        }
        sb.append("huy=").append(ds.getSoDaHuy()).append("|nhatKy=").append(ds.getNhatKySuCo().size());
        return sb.toString();
    }

    private static DaySchedule lichDuBonLoaiDaTre() {
        DaySchedule ds = new DaySchedule(LocalDate.now());
        ds.themItem(new FixedEvent("Học", LocalTime.of(8, 0), LocalTime.of(9, 0), PriorityLevel.BAT_BUOC));
        ds.themItem(new FlexibleTask("Đọc sách", LocalTime.of(9, 0), LocalTime.of(10, 0), PriorityLevel.CO_THE_BO));
        ds.themItem(new DeadlineTask("Nộp báo cáo", LocalTime.of(10, 0), LocalTime.of(11, 0), PriorityLevel.NEN_LAM,
                LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59))));
        ds.themItem(new RecurringEvent("Tập thể dục", LocalTime.of(11, 0), LocalTime.of(12, 0), PriorityLevel.NEN_LAM, 3));
        return ds;
    }

    @Test
    void khongTuReplan_quetViecDaTreNang_lichKhongDoi() {
        DaySchedule ds = lichDuBonLoaiDaTre();
        String truoc = chupLich(ds);
        // 15:00: cả 4 việc đều đã quá giờ bắt đầu >= 20 phút -> đủ điều kiện cảnh báo nặng
        List<CanhBaoViec> kq = BoTheoDoiLich.quet(ds, LocalTime.of(15, 0));
        assertEquals(4, kq.size());
        for (CanhBaoViec c : kq) {
            assertEquals(MucDoCanhBao.TRE_NANG, c.getMucDo());
        }
        assertEquals(truoc, chupLich(ds), "quet() không được đổi giờ, cờ hay bộ đếm của bất kỳ việc nào");
    }

    @Test
    void khongTuReplan_quetLapLaiMoiGiay_lichVanNguyen() {
        DaySchedule ds = lichDuBonLoaiDaTre();
        String truoc = chupLich(ds);
        // mô phỏng Timer 1 giây của MainFrame: quét liên tục suốt 2 tiếng
        for (int phut = 0; phut <= 120; phut++) {
            BoTheoDoiLich.quet(ds, LocalTime.of(9, 0).plusMinutes(phut));
            BoTheoDoiLich.quet(ds, LocalTime.of(9, 0).plusMinutes(phut), Set.of());
        }
        assertEquals(truoc, chupLich(ds));
    }

}
