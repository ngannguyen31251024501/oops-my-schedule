package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.Disruption;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * "Bộ não" của Oops! My Schedule — điều phối các {@link ReplanStrategy} cụ
 * thể (Strategy Pattern). Thuật toán chi tiết không còn nằm ở đây nữa (đã
 * chuyển sang {@link AbstractReplanStrategy} + 3 lớp con
 * {@link MinimizeDisruptionStrategy}, {@link DeadlineFirstStrategy},
 * {@link ReduceWorkloadStrategy}) — nhiệm vụ của ReplanEngine giờ chỉ còn là
 * chạy CẢ 3 chiến lược và trả về danh sách {@link PhuongAn} để người dùng tự
 * so sánh và chọn, thay vì áp đặt sẵn 1 "phương án tối ưu" duy nhất.
 * <p>
 * Không dùng AI hay API ngoài — mỗi chiến lược chỉ là 1 tập luật rule-based
 * khác nhau, nhưng đủ để tạo ra 3 kết quả THẬT SỰ khác nhau (không chỉ khác
 * tên gọi), đúng tinh thần "hệ thống đề xuất, người dùng quyết định".
 */
public class ReplanEngine {

    private final List<ReplanStrategy> cacChienLuoc = List.of(
        new MinimizeDisruptionStrategy(),
        new DeadlineFirstStrategy(),
        new ReduceWorkloadStrategy()
    );

    /**
     * Sinh ra 3 phương án thay thế cho 1 sự cố — mỗi phương án là kết quả
     * của 1 chiến lược khác nhau, đầy đủ (lịch trước/sau, danh sách thay
     * đổi, cảnh báo) chứ không chỉ là 1 hành động lẻ.
     */
    public List<PhuongAn> taoCacPhuongAn(DaySchedule ds, Disruption disruption, LocalTime thoiDiemHienTai) {
        // BR-11/BR-12: reject sớm thay vì để NullPointerException mù mờ nổ ra
        // giữa chừng thuật toán quét xung đột (khó truy vết).
        if (ds == null || disruption == null || thoiDiemHienTai == null) {
            throw new IllegalArgumentException("ds, disruption, thoiDiemHienTai không được null");
        }
        List<PhuongAn> ketQua = new ArrayList<>();
        for (ReplanStrategy chienLuoc : cacChienLuoc) {
            KetQuaSoSanh kq = chienLuoc.apDung(ds, disruption, thoiDiemHienTai);
            ketQua.add(new PhuongAn(chienLuoc.getTen(), chienLuoc.getBieuTuong(), chienLuoc.getMoTaNgan(), kq));
        }
        return ketQua;
    }

    /**
     * Giữ lại cho tương thích ngược (test cũ + bất kỳ nơi nào chỉ cần 1 kết
     * quả nhanh) — ủy quyền cho chiến lược "Ít thay đổi", đúng hành vi gốc
     * của ReplanEngine trước khi có 3 phương án.
     */
    public KetQuaSoSanh reSapXep(DaySchedule ds, Disruption disruption, LocalTime thoiDiemHienTai) {
        return new MinimizeDisruptionStrategy().apDung(ds, disruption, thoiDiemHienTai);
    }
}
