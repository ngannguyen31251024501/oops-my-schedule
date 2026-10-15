package com.oopsmyschedule.service;

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
            // TODO K-08: đổi sang timKhoangTrongDauTien(bi.layThoiLuong(), LocalTime.of(6, 0)) khi Manh push
            LocalTime slot = ngayMai.timKhoangTrongDauTien(bi.layThoiLuong(), LocalTime.of(6, 0));
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
        // TODO K-07: khi Manh có chuaXepLich trong DaySchedule: homNay.themChuaXepLich(bi) cho từng việc
        for (ScheduleItem v : chuaXepLich) {
            homNay(v);
        }
        return new KetQuaApDung(chuyenNgayMai, chuaXepLich);
    }
}