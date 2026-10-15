package com.oopsmyschedule.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Việc có hạn chót riêng: nộp báo cáo, chạy deadline môn An ninh thông tin /
 * Lập trình ứng dụng / Phân tích dữ liệu... Có thể dời trong ngày, MIỄN LÀ
 * còn kịp hạn chót ({@code hanChot}). Nếu dời sẽ khiến trễ hạn, hệ thống
 * KHÔNG tự dời mà chỉ đánh dấu cảnh báo để người dùng tự quyết định
 * (đúng theo TC-06 trong tài liệu thiết kế).
 * <p>
 * Ghi chú thiết kế: tài liệu gốc mô tả {@code hanChot} là {@code LocalDate},
 * nhưng để so sánh chính xác đến từng phút (ví dụ hạn "23:59 tối nay"), lớp
 * này lưu hạn chót dưới dạng {@link LocalDateTime} và có thêm
 * {@code ngayThucHien} (ngày mà việc này được lên lịch) để tính đúng khoảng
 * cách tới hạn.
 */
public class DeadlineTask extends ScheduleItem {

    private final LocalDateTime hanChot;
    private LocalDate ngayThucHien;
    private boolean canhBaoTreHan = false;

    public DeadlineTask(String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                        PriorityLevel doUuTien, LocalDateTime hanChot) {
        this(ten, gioBatDau, gioKetThuc, doUuTien, hanChot, LocalDate.now());
    }

    public DeadlineTask(String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                        PriorityLevel doUuTien, LocalDateTime hanChot, LocalDate ngayThucHien) {
        super(ten, gioBatDau, gioKetThuc, doUuTien);
        // INV-21: ngày thực hiện bắt buộc có (xuLyKhiBiTre dùng để tính hạn chót).
        if (ngayThucHien == null) {
            throw new IllegalArgumentException("Ngày thực hiện không được null.");
        }
        if (hanChot == null) {
            throw new IllegalArgumentException("Hạn chót không được null.");
        }
        // INV-06 (K-05): hạn chót không được trước giờ kết thúc của việc. Chỉ constructor
        // công khai này kiểm; constructor 9 tham số (đọc file) cố ý KHÔNG kiểm để vẫn mở được file cũ.
        if (hanChot.isBefore(LocalDateTime.of(ngayThucHien, gioKetThuc))) {
            throw new IllegalArgumentException("Hạn chót (" + hanChot
                    + ") không được trước giờ kết thúc (" + LocalDateTime.of(ngayThucHien, gioKetThuc) + ").");
        }
        this.hanChot = hanChot;
        this.ngayThucHien = ngayThucHien;
    }

    /** Copy constructor giữ nguyên id + trạng thái (xem javadoc ở ScheduleItem). */
    public DeadlineTask(String id, String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                        PriorityLevel doUuTien, boolean hoanThanh, LocalDateTime hanChot,
                        LocalDate ngayThucHien, boolean canhBaoTreHan) {
        super(id, ten, gioBatDau, gioKetThuc, doUuTien, hoanThanh);
        if (hanChot == null) {
            throw new IllegalArgumentException("Hạn chót không được null.");
        }
        this.hanChot = hanChot;
        this.ngayThucHien = ngayThucHien;
        this.canhBaoTreHan = canhBaoTreHan;
    }

    @Override
    public boolean coTheDoiLich() {
        return true;
    }

    @Override
    public void xuLyKhiBiTre(int soPhutTre) {
        // Cộng phút vào LocalDateTime đầy đủ (không phải chỉ LocalTime) để không mất
        // thông tin "qua ngày mới" khi soPhutTre lớn (LocalTime.plusMinutes tự cuộn
        // vòng trong 24h nên nếu cộng trước rồi mới ghép ngày sẽ tính sai ngày).
        LocalDateTime ketThucMoi = LocalDateTime.of(ngayThucHien, getGioKetThuc()).plusMinutes(soPhutTre);
        if (ketThucMoi.isAfter(hanChot)) {
            // Không tự dời — chỉ cảnh báo, để người dùng tự quyết định
            // (TC-06: hạn 23:59 tối nay, đang 22:00, nếu dời sang ngày mai sẽ trễ hạn).
            canhBaoTreHan = true;
            return;
        }
        doiLich(soPhutTre);
        canhBaoTreHan = false;
    }

    public boolean isCanhBaoTreHan() {
        return canhBaoTreHan;
    }

    /**
     * FIX BUG "Deadline warning lệch state": trước đây khi
     * {@code ReplanEngine} (qua {@code ganLaiKhungGioBoiEngine}) ép dời giờ
     * kết thúc của DeadlineTask, cờ {@code canhBaoTreHan} KHÔNG được cập
     * nhật lại — chỉ có {@link #xuLyKhiBiTre} (đường đi "tự quyết" của
     * riêng item) mới set cờ này. Kết quả: UI có thể hiển thị "đã quá hạn"
     * (vì {@code getGioKetThuc()} sau hạn chót) trong khi object vẫn giữ
     * {@code canhBaoTreHan = false}. Gọi phương thức này ngay sau mỗi lần
     * engine ép đặt lại khung giờ để đồng bộ lại cờ theo giờ kết thúc HIỆN
     * TẠI, không đụng tới giờ giấc (chỉ tính lại cờ).
     */
    public void dongBoCanhBaoTheoKhungGioHienTai() {
        LocalDateTime ketThucHienTai = LocalDateTime.of(ngayThucHien, getGioKetThuc());
        this.canhBaoTreHan = ketThucHienTai.isAfter(hanChot);
    }

    public LocalDateTime getHanChot() {
        return hanChot;
    }

    public void setNgayThucHien(LocalDate ngayThucHien) {
        this.ngayThucHien = ngayThucHien;
    }

    public LocalDate getNgayThucHien() {
        return ngayThucHien;
    }

    @Override
    public String moTaTrangThai() {
        return canhBaoTreHan
                ? "⚠ CẢNH BÁO: dời sẽ trễ hạn (" + hanChot + ") — giữ nguyên, chờ bạn quyết định"
                : "Còn kịp hạn (" + hanChot + ")";
    }

    @Override
    public String loaiViec() {
        return "Deadline";
    }
}