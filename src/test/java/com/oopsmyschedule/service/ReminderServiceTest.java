package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.DeadlineTask;
import com.oopsmyschedule.domain.FlexibleTask;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.ScheduleItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** [NG-03b] ReminderService: nhắc sắp tới giờ (CT-12), mốc deadline, không nhắc lặp, chỉ đọc lịch (BR-03). */
class ReminderServiceTest {

    private static final LocalDate NGAY = LocalDate.of(2026, 10, 14);

    private static DaySchedule lichMotViec(ScheduleItem item) {
        DaySchedule ds = new DaySchedule(NGAY);
        ds.themItem(item);
        return ds;
    }

    private static FlexibleTask viecLuc14h() {
        return new FlexibleTask("Họp nhóm", LocalTime.of(14, 0), LocalTime.of(15, 0), PriorityLevel.NEN_LAM);
    }

    private static List<String> quetDeadline(long phutConLai) {
        // Hạn chót cố định 23:59 (>= giờ kết thúc, INV-06); thay đổi "bây giờ" để có r phút còn lại.
        LocalDateTime han = LocalDateTime.of(NGAY, LocalTime.of(23, 59));
        DeadlineTask dt = new DeadlineTask("Nộp báo cáo", LocalTime.of(20, 0), LocalTime.of(21, 0),
                PriorityLevel.NEN_LAM, han, NGAY);
        LocalDateTime bayGio = han.minusMinutes(phutConLai);
        // việc bắt đầu 20:00; các r dùng trong test đều nằm ngoài cửa sổ 15' trước 20:00 nên không dính nhắc "sắp tới"
        DaySchedule ds = lichMotViec(dt);
        ReminderService rs = new ReminderService();
        return rs.quet(ds, bayGio.toLocalDate(), bayGio.toLocalTime());
    }

    // ---------- khởi tạo ----------

    // ---------- nhắc sắp tới giờ (CT-12) ----------

    @Test
    void conMuoiPhut_nhacMotLan() {
        DaySchedule ds = lichMotViec(viecLuc14h());
        ReminderService rs = new ReminderService();
        List<String> kq = rs.quet(ds, NGAY, LocalTime.of(13, 50));
        assertEquals(1, kq.size());
        assertTrue(kq.get(0).contains("Sắp tới"));
        assertTrue(kq.get(0).contains("Họp nhóm"));
        assertTrue(kq.get(0).contains("14:00"));
    }

    @Test
    void khongNhacLap_quetLaiCungViecThiRong() {
        DaySchedule ds = lichMotViec(viecLuc14h());
        ReminderService rs = new ReminderService();
        assertEquals(1, rs.quet(ds, NGAY, LocalTime.of(13, 50)).size());
        assertTrue(rs.quet(ds, NGAY, LocalTime.of(13, 50)).isEmpty());
        assertTrue(rs.quet(ds, NGAY, LocalTime.of(13, 55)).isEmpty());
        assertTrue(rs.quet(ds, NGAY, LocalTime.of(14, 0)).isEmpty());
    }

    @Test
    void bienNgayThongBao_15phutNhac_16phutChua_0phutNhac_amMotPhutKhong() {
        assertEquals(1, new ReminderService().quet(lichMotViec(viecLuc14h()), NGAY, LocalTime.of(13, 45)).size()); // còn 15'
        assertTrue(new ReminderService().quet(lichMotViec(viecLuc14h()), NGAY, LocalTime.of(13, 44)).isEmpty());   // còn 16'
        assertEquals(1, new ReminderService().quet(lichMotViec(viecLuc14h()), NGAY, LocalTime.of(14, 0)).size());  // còn 0'
        assertTrue(new ReminderService().quet(lichMotViec(viecLuc14h()), NGAY, LocalTime.of(14, 1)).isEmpty());    // đã qua giờ -> BoTheoDoiLich lo
    }

    @Test
    void viecDaHoanThanhKhongNhac() {
        ScheduleItem v = viecLuc14h();
        v.danhDauHoanThanh();
        assertTrue(new ReminderService().quet(lichMotViec(v), NGAY, LocalTime.of(13, 55)).isEmpty());
    }

    // ---------- mốc deadline gần kề: 24h / 3h / 1h (CT-12) ----------

    @Test
    void deadline_xaHon24Tieng_chuaNhac() {
        assertTrue(quetDeadline(1500).isEmpty());
        assertTrue(quetDeadline(1441).isEmpty());
    }

    @Test
    void deadline_moc24Tieng_1440va181() {
        for (long r : new long[]{1440, 181}) {
            List<String> kq = quetDeadline(r);
            assertEquals(1, kq.size(), "r=" + r);
            assertTrue(kq.get(0).contains("24 tiếng"), "r=" + r);
        }
    }

    @Test
    void deadline_moc3Tieng_180va61_khongBaoNhamMoc24() {
        for (long r : new long[]{180, 61}) {
            List<String> kq = quetDeadline(r);
            assertEquals(1, kq.size(), "r=" + r);
            assertTrue(kq.get(0).contains("3 tiếng"), "r=" + r);
            assertFalse(kq.get(0).contains("24 tiếng"), "r=" + r);
        }
    }

    @Test
    void deadline_moc1Tieng_60va1() {
        for (long r : new long[]{60, 1}) {
            List<String> kq = quetDeadline(r);
            assertEquals(1, kq.size(), "r=" + r);
            assertTrue(kq.get(0).contains("1 tiếng") || kq.get(0).contains("60 phút"), "r=" + r + " -> " + kq.get(0));
        }
    }

    @Test
    void deadline_daQuaHanHoacDungHan_khongNhac() {
        assertTrue(quetDeadline(0).isEmpty());
        assertTrue(quetDeadline(-10).isEmpty());
    }

    @Test
    void deadline_moiMocChiNhacMotLan() {
        LocalDateTime han = LocalDateTime.of(NGAY, LocalTime.of(23, 59));
        DeadlineTask dt = new DeadlineTask("Nộp báo cáo", LocalTime.of(20, 0), LocalTime.of(21, 0),
                PriorityLevel.NEN_LAM, han, NGAY);
        DaySchedule ds = lichMotViec(dt);
        ReminderService rs = new ReminderService();
        assertEquals(1, rs.quet(ds, NGAY, LocalTime.of(23, 0)).size());   // còn 59' -> mốc 1 tiếng
        assertTrue(rs.quet(ds, NGAY, LocalTime.of(23, 1)).isEmpty());     // vẫn trong dải 1 tiếng -> không lặp
        assertTrue(rs.quet(ds, NGAY, LocalTime.of(23, 2)).isEmpty());
    }

    @Test
    void deadline_viecDaHoanThanhKhongNhacMoc() {
        LocalDateTime han = LocalDateTime.of(NGAY, LocalTime.of(23, 59));
        DeadlineTask dt = new DeadlineTask("Nộp báo cáo", LocalTime.of(20, 0), LocalTime.of(21, 0),
                PriorityLevel.NEN_LAM, han, NGAY);
        dt.danhDauHoanThanh();
        assertTrue(new ReminderService().quet(lichMotViec(dt), NGAY, LocalTime.of(23, 0)).isEmpty());
    }

    // ---------- chỉ đọc lịch, không tự Replan (BR-03) ----------

    @Test
    void quetKhongDoiLich() {
        DaySchedule ds = new DaySchedule(NGAY);
        FlexibleTask f = viecLuc14h();
        ds.themItem(f);
        LocalTime batDau = f.getGioBatDau();
        LocalTime ketThuc = f.getGioKetThuc();
        ReminderService rs = new ReminderService();
        for (int p = 0; p < 24 * 60; p += 5) {
            rs.quet(ds, NGAY, LocalTime.of(0, 0).plusMinutes(p));
        }
        assertEquals(1, ds.layLichHienTai().size());
        assertEquals(batDau, f.getGioBatDau());
        assertEquals(ketThuc, f.getGioKetThuc());
        assertEquals(0, f.getSoLanDaDoi());
        assertFalse(f.isHoanThanh());
        assertEquals(0, ds.getSoDaHuy());
        assertTrue(ds.getNhatKySuCo().isEmpty());
    }
}
