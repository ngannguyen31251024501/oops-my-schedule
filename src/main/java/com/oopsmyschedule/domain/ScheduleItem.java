package com.oopsmyschedule.domain;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Lớp cha trừu tượng cho mọi loại việc trong lịch trình 1 ngày.
 * Trừu tượng: coTheDoiLich(), xuLyKhiBiTre(int) để lớp con tự định nghĩa.
 * Đa hình: ReplanEngine gọi item.xuLyKhiBiTre(soPhut) mà không cần biết loại item.
 * Đóng gói: gioBatDau, gioKetThuc private, chỉ đổi qua doiLich(int) / datLaiKhungGio(...).
 */
public abstract class ScheduleItem {

    /** Mốc cuối ngày hợp lệ (23:59) tính bằng giây: 86340. */
    private static final long CUOI_NGAY_GIAY = LocalTime.of(23, 59).toSecondOfDay();

    private final String id;
    private String ten;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private PriorityLevel doUuTien;
    private boolean hoanThanh = false;

    protected ScheduleItem(String ten, LocalTime gioBatDau, LocalTime gioKetThuc, PriorityLevel doUuTien) {
        this(UUID.randomUUID().toString(), ten, gioBatDau, gioKetThuc, doUuTien, false);
    }

    /**
     * Constructor dùng khi SAO CHÉP 1 item đã có (xem DaySchedule#saoChep()).
     * Giữ nguyên id và trạng thái hoàn thành gốc để SkipDisruption đối chiếu được.
     */
    protected ScheduleItem(String id, String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                           PriorityLevel doUuTien, boolean hoanThanh) {
        if (gioBatDau == null || gioKetThuc == null) {
            throw new IllegalArgumentException("Giờ bắt đầu / giờ kết thúc không được null.");
        }
        // INV-15: thời gian không có giây
        kiemTraKhongCoGiay(gioBatDau);
        kiemTraKhongCoGiay(gioKetThuc);
        if (!gioBatDau.isBefore(gioKetThuc)) {
            throw new IllegalArgumentException(
                    "Giờ bắt đầu (" + gioBatDau + ") phải trước giờ kết thúc (" + gioKetThuc + ").");
        }
        if (ten == null || ten.isBlank()) {
            throw new IllegalArgumentException("Tên việc không được để trống.");
        }
        this.id = Objects.requireNonNull(id, "id không được null.");
        this.ten = ten;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.doUuTien = Objects.requireNonNull(doUuTien, "Độ ưu tiên không được null.");
        this.hoanThanh = hoanThanh;
    }

    // ---------- Hành vi đa hình: mỗi lớp con override khác nhau ----------

    /** Việc này có được phép dời giờ không? Mỗi loại quyết định khác nhau. */
    public abstract boolean coTheDoiLich();

    /** Phản ứng khi bị trễ {@code soPhutTre} phút (dời giờ, ném lỗi, kiểm tra hạn chót, hay bỏ qua). */
    public abstract void xuLyKhiBiTre(int soPhutTre);

    /** Mô tả ngắn gọn lý do/kết quả xử lý gần nhất, dùng ở bảng so sánh. */
    public abstract String moTaTrangThai();

    // ---------- Helper ----------

    /** INV-15: ném IllegalArgumentException nếu giờ có giây hoặc nano giây. */
    private static void kiemTraKhongCoGiay(LocalTime gio) {
        if (gio.getSecond() != 0 || gio.getNano() != 0) {
            throw new IllegalArgumentException("Giờ " + gio + " không được có giây.");
        }
    }

    /**
     * Dời {@code soPhut} phút có làm khung giờ vượt ra ngoài 00:00–23:59 không?
     * Tính bằng giây tuyệt đối vì LocalTime.plusMinutes cuộn vòng qua nửa đêm.
     * Kết thúc đúng 23:59 vẫn hợp lệ. Chỉ đọc, không đổi trạng thái.
     */
    public boolean vuotNuaDemNeuDoi(int soPhut) {
        long batDauMoi = gioBatDau.toSecondOfDay() + soPhut * 60L;
        long ketThucMoi = gioKetThuc.toSecondOfDay() + soPhut * 60L;
        return batDauMoi < 0 || ketThucMoi > CUOI_NGAY_GIAY;
    }

    /**
     * Dời cả gioBatDau và gioKetThuc đi {@code soPhut} phút, giữ nguyên thời lượng.
     * @throws IllegalStateException    nếu việc đã hoàn thành (INV-18)
     * @throws IllegalArgumentException nếu dời xong bị vượt 23:59 (INV-03)
     */
    protected void doiLich(int soPhut) {
        if (hoanThanh) {
            throw new IllegalStateException("Việc \"" + ten + "\" đã hoàn thành, không thể dời lịch.");
        }
        // INV-03: guard TRƯỚC khi đổi bất kỳ field nào, để object không bị đổi nửa chừng
        if (vuotNuaDemNeuDoi(soPhut)) {
            throw new IllegalArgumentException(
                    "Dời " + soPhut + " phút sẽ làm giờ kết thúc (" + gioKetThuc + ") vượt 23:59.");
        }
        this.gioBatDau = this.gioBatDau.plusMinutes(soPhut);
        this.gioKetThuc = this.gioKetThuc.plusMinutes(soPhut);
    }

    /**
     * Đặt lại khung giờ tuyệt đối (ReplanEngine dùng khi cần "ép" 1 vị trí mới).
     * @throws IllegalStateException    nếu việc đã hoàn thành (INV-18)
     * @throws IllegalArgumentException nếu giờ null, có giây, hoặc bắt đầu không trước kết thúc
     */
    protected void datLaiKhungGio(LocalTime moiBatDau, LocalTime moiKetThuc) {
        if (hoanThanh) {
            throw new IllegalStateException("Việc \"" + ten + "\" đã hoàn thành, không thể đổi giờ.");
        }
        if (moiBatDau == null || moiKetThuc == null) {
            throw new IllegalArgumentException("Khung giờ mới không được null.");
        }
        kiemTraKhongCoGiay(moiBatDau);
        kiemTraKhongCoGiay(moiKetThuc);
        if (!moiBatDau.isBefore(moiKetThuc)) {
            throw new IllegalArgumentException("Khung giờ mới không hợp lệ: " + moiBatDau + " - " + moiKetThuc);
        }
        this.gioBatDau = moiBatDau;
        this.gioKetThuc = moiKetThuc;
    }

    /**
     * Cho phép ReplanEngine gán khung giờ mới sau khi đã phân tích ưu tiên/xung đột toàn cục.
     * Vẫn đi qua guard clause của datLaiKhungGio (lớp con có thể override để chặn thêm).
     */
    public final void ganLaiKhungGioBoiEngine(LocalTime moiBatDau, LocalTime moiKetThuc) {
        datLaiKhungGio(moiBatDau, moiKetThuc);
    }

    public Duration layThoiLuong() {
        return Duration.between(gioBatDau, gioKetThuc);
    }

    public boolean trung(ScheduleItem khac) {
        return this.gioBatDau.isBefore(khac.gioKetThuc) && khac.gioBatDau.isBefore(this.gioKetThuc);
    }

    // ---------- Getters / setters đơn giản (Encapsulation) ----------

    public String getId() {
        return id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        if (ten == null || ten.isBlank()) {
            throw new IllegalArgumentException("Tên việc không được để trống.");
        }
        this.ten = ten;
    }

    public LocalTime getGioBatDau() {
        return gioBatDau;
    }

    public LocalTime getGioKetThuc() {
        return gioKetThuc;
    }

    public PriorityLevel getDoUuTien() {
        return doUuTien;
    }

    public void setDoUuTien(PriorityLevel doUuTien) {
        this.doUuTien = Objects.requireNonNull(doUuTien);
    }

    /** Tên loại việc (dùng cho hiển thị UI + lưu JSON), ví dụ "Cố định", "Linh hoạt". */
    public abstract String loaiViec();

    public boolean isHoanThanh() {
        return hoanThanh;
    }

    /** Đánh dấu việc đã hoàn thành. */
    public void danhDauHoanThanh() {
        this.hoanThanh = true;
    }

    public void boDanhDauHoanThanh() {
        this.hoanThanh = false;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s - %s) - %s", loaiViec(), ten, gioBatDau, gioKetThuc, doUuTien);
    }

    /** Đồng nhất theo id, KHÔNG theo nội dung (giờ/tên bị engine chỉnh liên tục). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleItem other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}