package com.oopsmyschedule.service;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * 1 dòng "nhật ký sự cố" — ghi lại: lúc nào có sự cố gì xảy ra, người dùng
 * chọn phương án nào, và Replan Engine đã thay đổi những việc nào để xử lý.
 * <p>
 * Dùng để dựng mục "🔎 Vì sao hôm nay chưa đạt" trong Tổng kết ngày — liên
 * kết trực tiếp lịch sử Replan Engine vào Daily Summary, thay vì Tổng kết
 * chỉ dừng ở vài con số khô khan.
 */
public class SuCoNhatKy {

    private final LocalTime gio;
    private final String moTaSuCo;
    private final String tenPhuongAn;
    private final List<String> danhSachThayDoi;
    private final List<String> tenKhongTheSapXep;

    public SuCoNhatKy(LocalTime gio, String moTaSuCo, String tenPhuongAn,
                       List<String> danhSachThayDoi, List<String> tenKhongTheSapXep) {
        this.gio = gio;
        this.moTaSuCo = moTaSuCo;
        this.tenPhuongAn = tenPhuongAn;
        this.danhSachThayDoi = List.copyOf(danhSachThayDoi);
        this.tenKhongTheSapXep = tenKhongTheSapXep == null ? List.of() : List.copyOf(tenKhongTheSapXep);
    }

    public LocalTime getGio() {
        return gio;
    }

    public String getMoTaSuCo() {
        return moTaSuCo;
    }

    public String getTenPhuongAn() {
        return tenPhuongAn;
    }

    /** Danh sách thay đổi cụ thể (VD: "Làm bài tập → Dời sang 16:00"), đã lọc bỏ các dòng "Không có việc nào...". */
    public List<String> getDanhSachThayDoiThucSu() {
        return Collections.unmodifiableList(danhSachThayDoi.stream()
            .filter(s -> !s.startsWith("Không có việc nào"))
            .toList());
    }

    public List<String> getTenKhongTheSapXep() {
        return tenKhongTheSapXep;
    }
}
