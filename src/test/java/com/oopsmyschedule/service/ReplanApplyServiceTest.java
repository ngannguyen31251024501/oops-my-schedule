package com.oopsmyschedule.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.FlexibleTask;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.ScheduleItem;

/** T-12/T-13: áp dụng phương án + chuyển ngày mai (BR-02 tạm, BR-04). */
class ReplanApplyServiceTest {

    private static final LocalDate HOM_NAY = LocalDate.of(2026, 10, 12);
    private static final LocalDate NGAY_MAI = HOM_NAY.plusDays(1);

    private final ReplanApplyService service = new ReplanApplyService();

    private static FixedEvent fixed(String ten, int gioBd, int phutBd, int gioKt, int phutKt) {
        return new FixedEvent(ten, LocalTime.of(gioBd, phutBd), LocalTime.of(gioKt, phutKt),
                PriorityLevel.BAT_BUOC);
    }

    /** Việc 60 phút (10:00-11:00) bị đề xuất bỏ. */
    private static FlexibleTask viecBiBo() {
        return new FlexibleTask("Doc sach", LocalTime.of(10, 0), LocalTime.of(11, 0),
                PriorityLevel.CO_THE_BO);
    }

    /** Phương án: lịch sau chỉ còn Fixed 08-09, "Doc sach" bị đề xuất bỏ. */
    private PhuongAn taoPhuongAn(DaySchedule homNay, ScheduleItem viecBo) {
        DaySchedule lichSau = new DaySchedule(HOM_NAY);
        lichSau.themItem(fixed("Hoc", 8, 0, 9, 0));
        KetQuaSoSanh ketQua = new KetQuaSoSanh(homNay.saoChep(), lichSau);
        ketQua.themDeXuatBo(viecBo);
        return new PhuongAn("Giam tai", "x", "Bo viec tre", ketQua);
    }

    private DaySchedule lichHomNay(ScheduleItem viecBo) {
        DaySchedule homNay = new DaySchedule(HOM_NAY);
        homNay.themItem(fixed("Hoc", 8, 0, 9, 0));
        homNay.themItem(viecBo);
        return homNay;
    }

    private static ScheduleItem timTheoTen(DaySchedule ds, String ten) {
        return ds.layLichHienTai().stream()
                .filter(i -> i.getTen().equals(ten)).findFirst().orElse(null);
    }

    @Test
    void ngayMaiTrong_viecChuyenSangNgayMai() {
        FlexibleTask bi = viecBiBo();
        DaySchedule homNay = lichHomNay(bi);
        DaySchedule ngayMai = new DaySchedule(NGAY_MAI);

        KetQuaApDung kq = service.apDung(homNay, ngayMai, taoPhuongAn(homNay, bi));

        assertEquals(1, kq.getChuyenNgayMai().size());
        assertTrue(kq.getChuaXepLich().isEmpty());
        // BR-04: lịch hôm nay = lịch sau của phương án (việc bị bỏ không còn)
        assertNull(timTheoTen(homNay, "Doc sach"));
        assertNotNull(timTheoTen(homNay, "Hoc"));
        // việc đã sang ngày mai, đủ 60 phút (không nén thời lượng, BR-01)
        ScheduleItem daChuyen = timTheoTen(ngayMai, "Doc sach");
        assertNotNull(daChuyen);
        assertEquals(60, daChuyen.layThoiLuong().toMinutes());
    }

    @Test
    void khePhutVuaDu_viecVaoDungKhe() {
        FlexibleTask bi = viecBiBo();
        DaySchedule homNay = lichHomNay(bi);
        // Ngày mai chỉ còn đúng 1 khe 10:00-11:00 (60 phút)
        DaySchedule ngayMai = new DaySchedule(NGAY_MAI);
        ngayMai.themItem(fixed("Ban 1", 0, 0, 10, 0));
        ngayMai.themItem(fixed("Ban 2", 11, 0, 23, 59));

        KetQuaApDung kq = service.apDung(homNay, ngayMai, taoPhuongAn(homNay, bi));

        assertEquals(1, kq.getChuyenNgayMai().size());
        assertTrue(kq.getChuaXepLich().isEmpty());
        ScheduleItem daChuyen = timTheoTen(ngayMai, "Doc sach");
        assertNotNull(daChuyen);
        assertEquals(LocalTime.of(10, 0), daChuyen.getGioBatDau());
    }

    @Test
    void ngayMaiKin_viecVaoChuaXepLich_khongMat() {
        FlexibleTask bi = viecBiBo();
        DaySchedule homNay = lichHomNay(bi);
        DaySchedule ngayMai = new DaySchedule(NGAY_MAI);
        ngayMai.themItem(fixed("Kin ca ngay", 0, 0, 23, 59));

        KetQuaApDung kq = service.apDung(homNay, ngayMai, taoPhuongAn(homNay, bi));

        // BR-02 (tạm): không chuyển được thì vào chuaXepLich, không biến mất
        assertTrue(kq.getChuyenNgayMai().isEmpty());
        assertEquals(1, kq.getChuaXepLich().size());
        assertSame(bi, kq.getChuaXepLich().get(0));
        // ngày mai không bị nhét thêm (không chồng giờ)
        assertEquals(1, ngayMai.layLichHienTai().size());
    }
}