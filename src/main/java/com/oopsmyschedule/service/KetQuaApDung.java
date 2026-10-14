package com.oopsmyschedule.service;

import java.util.Collections;
import java.util.List;

import com.oopsmyschedule.domain.ScheduleItem;

/** Kết quả sau khi áp dụng một PhuongAn (BR-02, BR-04). */
public class KetQuaApDung {
    private final List<ScheduleItem> chuyenNgayMai;
    private final List<ScheduleItem> chuaXepLich;

    public KetQuaApDung(List<ScheduleItem> chuyenNgayMai,
                        List<ScheduleItem> chuaXepLich) {
        if (chuyenNgayMai == null || chuaXepLich == null) {
            throw new IllegalArgumentException("Danh sách kết quả không được null");
        }
        this.chuyenNgayMai = List.copyOf(chuyenNgayMai);
        this.chuaXepLich = List.copyOf(chuaXepLich);
    }

    public static KetQuaApDung rong() {
        return new KetQuaApDung(Collections.emptyList(), Collections.emptyList());
    }

    public List<ScheduleItem> getChuyenNgayMai() { return chuyenNgayMai; }
    public List<ScheduleItem> getChuaXepLich()   { return chuaXepLich; }
}