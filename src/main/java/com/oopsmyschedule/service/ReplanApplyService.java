package com.oopsmyschedule.service;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.oopsmyschedule.domain.ScheduleItem;

/**
 * Áp dụng phương án được chọn vào lịch thật (BR-04).
 * Việc không chuyển được sang ngày mai thì đưa vào chuaXepLich, không xóa (BR-02 tạm).
 * Không gọi KhoDuLieu ở đây, việc lưu file do Controller làm.
 */
public class ReplanApplyService {

    public KetQuaApDung apDung(DaySchedule homNay, DaySchedule ngayMai, PhuongAn phuongAn) {
        if (homNay == null || ngayMai == null || phuongAn == null) {
            throw new IllegalArgumentException("homNay, ngayMai, phuongAn không được null");
        }
        KetQuaSoSanh ketQua = phuongAn.getKetQua();
        homNay.thayTheToanBo(ketQua.getLichSau().layLichHienTai());

        List<ScheduleItem> chuyenNgayMai = new ArrayList<>();
        List<ScheduleItem> chuaXepLich = new ArrayList<>();

        for (ScheduleItem bi : ketQua.getDanhSachDeXuatBo()) {
            LocalTime slot = timKheTu(ngayMai, bi.layThoiLuong(), LocalTime.of(6, 0)); // TODO: đổi sang hàm của Manh khi merge
            if (slot == null) {
                chuaXepLich.add(bi);
                continue;
            }
            try {
                ngayMai.themItem(DaySchedule.taoBanSaoSangNgayKhac(bi, ngayMai.getNgay(), slot));
                chuyenNgayMai.add(bi);
            } catch (IllegalArgumentException ex) {
                chuaXepLich.add(bi);
            }
        }
        for (ScheduleItem v : chuaXepLich) {
            homNay.themChuaXepLich(v);
        }
        return new KetQuaApDung(chuyenNgayMai, chuaXepLich);
    }
    /** Dò khe trống LIÊN TỤC đủ dài đầu tiên kể từ tuGio, đến hết 23:59; không có thì trả null. */
    private static LocalTime timKheTu(DaySchedule ds, Duration thoiLuong, LocalTime tuGio) {
        long can = thoiLuong.toMinutes();
        LocalTime conTro = tuGio;
        for (ScheduleItem it : ds.layLichHienTai()) { // đã sắp theo giờ bắt đầu
            if (!it.getGioKetThuc().isAfter(conTro)) {
                continue; // việc này kết thúc trước con trỏ, bỏ qua
            }
            if (it.getGioBatDau().isAfter(conTro)
                    && Duration.between(conTro, it.getGioBatDau()).toMinutes() >= can) {
                return conTro;
            }
            conTro = it.getGioKetThuc();
        }
        LocalTime cuoiNgay = LocalTime.of(23, 59);
        if (conTro.isBefore(cuoiNgay)
                && Duration.between(conTro, cuoiNgay).toMinutes() >= can) {
            return conTro;
        }
        return null;
    }
}