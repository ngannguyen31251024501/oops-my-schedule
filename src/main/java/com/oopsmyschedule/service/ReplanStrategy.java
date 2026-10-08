package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.Disruption;

import java.time.LocalTime;

/**
 * Chiến lược tái sắp xếp lịch (Strategy Pattern). Trước đây {@code ReplanEngine}
 * chỉ tính ra 1 phương án "tối ưu" duy nhất — điều này khiến người dùng không
 * có quyền lựa chọn (giống app tự quyết định thay họ). Giờ mỗi implementation
 * của interface này đại diện cho 1 "khẩu vị" đánh đổi khác nhau khi lịch
 * không đủ chỗ cho mọi việc (giữ nguyên nhiều nhất? bảo vệ deadline trước?
 * hay chủ động giảm tải?) — {@code ReplanEngine} chạy CẢ 3 rồi để người dùng
 * tự so sánh & chọn (xem {@link PhuongAn}).
 * <p>
 * Cố tình không dùng AI/API ngoài — mỗi chiến lược chỉ là 1 tập luật
 * (rule-based) khác nhau, đủ để thể hiện Đa hình (Polymorphism) rõ ràng
 * trong UML mà không cần thuật toán phức tạp.
 */
public interface ReplanStrategy {

    /** Tên ngắn gọn hiển thị trên UI, ví dụ "Ít thay đổi". */
    String getTen();

    /** 1 emoji đại diện cho chiến lược, dùng làm biểu tượng trên thẻ phương án. */
    String getBieuTuong();

    /** Mô tả ngắn (1 câu) giải thích chiến lược này ưu tiên điều gì. */
    String getMoTaNgan();

    /** Chạy chiến lược này trên 1 lịch + 1 sự cố, trả về kết quả so sánh trước/sau. */
    KetQuaSoSanh apDung(DaySchedule ds, Disruption disruption, LocalTime thoiDiemHienTai);
}
