package com.oopsmyschedule.controller;

import com.oopsmyschedule.domain.Disruption;
import com.oopsmyschedule.domain.ScheduleItem;
import com.oopsmyschedule.service.DaySchedule;
import com.oopsmyschedule.service.KetQuaSoSanh;
import com.oopsmyschedule.service.KhoDuLieu;
import com.oopsmyschedule.service.PhuongAn;
import com.oopsmyschedule.service.ReplanEngine;
import com.oopsmyschedule.service.SuCoNhatKy;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Điều phối việc tạo và áp dụng phương án sắp xếp lại lịch.
 */
public class ReplanController {

    private final ReplanEngine engine;
    private final Function<LocalDate, DaySchedule> layLich;

    public ReplanController(
            ReplanEngine engine,
            Function<LocalDate, DaySchedule> layLich) {
        this.engine = Objects.requireNonNull(engine);
        this.layLich = Objects.requireNonNull(layLich);
    }

    /**
     * Tạo các phương án để giao diện hiển thị.
     * Không thay đổi lịch gốc tại bước này.
     */
    public List<PhuongAn> taoPhuongAn(
            DaySchedule lichHomNay,
            Disruption suCo,
            LocalTime thoiDiemHienTai) {

        Objects.requireNonNull(lichHomNay);
        Objects.requireNonNull(suCo);
        Objects.requireNonNull(thoiDiemHienTai);

        return engine.taoCacPhuongAn(
                lichHomNay, suCo, thoiDiemHienTai);
    }

    /**
     * Áp dụng phương án đã được người dùng chọn.
     * Chỉ gọi phương thức này sau khi người dùng xác nhận lựa chọn.
     */
    public KetQuaApDung apDungPhuongAn(
            PhuongAn phuongAn,
            DaySchedule lichHomNay,
            LocalTime thoiDiemHienTai,
            String tieuDeSuCo) throws IOException {

        Objects.requireNonNull(phuongAn);
        Objects.requireNonNull(lichHomNay);
        Objects.requireNonNull(thoiDiemHienTai);

        KetQuaSoSanh ketQua = phuongAn.getKetQua();

        if (ketQua == null || ketQua.getLichSau() == null) {
            throw new IllegalArgumentException(
                    "Phương án không có lịch kết quả hợp lệ.");
        }

        LocalDate ngayMai = lichHomNay.getNgay().plusDays(1);
        DaySchedule lichNgayMai = layLich.apply(ngayMai);

        if (lichNgayMai == null) {
            throw new IllegalStateException(
                    "Không tìm thấy lịch ngày mai.");
        }

        // Áp dụng lịch sau của phương án đã chọn.
        lichHomNay.thayTheToanBo(
                ketQua.getLichSau().layLichHienTai());

        int soChuyenNgayMai = 0;
        List<String> tenKhongTheSapXep = new ArrayList<>();

        // Cố gắng chuyển các việc được đề xuất bỏ sang ngày mai.
        for (ScheduleItem item : ketQua.getDanhSachDeXuatBo()) {
            LocalTime slotTrong =
                    lichNgayMai.timKhoangTrongDauTien(
                            item.layThoiLuong());

            if (slotTrong == null) {
                tenKhongTheSapXep.add(item.getTen());
                continue;
            }

            try {
                lichNgayMai.themItem(
                        DaySchedule.taoBanSaoSangNgayKhac(
                                item, ngayMai, slotTrong));
                soChuyenNgayMai++;
            } catch (IllegalArgumentException ex) {
                tenKhongTheSapXep.add(item.getTen());
            }
        }

        // Ghi nhật ký sự cố.
        lichHomNay.ghiNhatKySuCo(
                new SuCoNhatKy(
                        thoiDiemHienTai,
                        tieuDeSuCo,
                        phuongAn.getTen(),
                        ketQua.getDanhSachThayDoi(),
                        tenKhongTheSapXep));

        // Lưu rõ ràng cả hai ngày.
        KhoDuLieu.luuNgay(lichHomNay);
        KhoDuLieu.luuNgay(lichNgayMai);

        return new KetQuaApDung(
                ketQua.getDanhSachThayDoi().size(),
                soChuyenNgayMai,
                tenKhongTheSapXep);
    }

    public static class KetQuaApDung {
        private final int soThayDoi;
        private final int soChuyenNgayMai;
        private final List<String> tenKhongTheSapXep;

        public KetQuaApDung(
                int soThayDoi,
                int soChuyenNgayMai,
                List<String> tenKhongTheSapXep) {
            this.soThayDoi = soThayDoi;
            this.soChuyenNgayMai = soChuyenNgayMai;
            this.tenKhongTheSapXep =
                    List.copyOf(tenKhongTheSapXep);
        }

        public int getSoThayDoi() {
            return soThayDoi;
        }

        public int getSoChuyenNgayMai() {
            return soChuyenNgayMai;
        }

        public List<String> getTenKhongTheSapXep() {
            return tenKhongTheSapXep;
        }
    }
}