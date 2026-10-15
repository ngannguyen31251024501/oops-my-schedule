package com.oopsmyschedule.domain;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Lớp cha trừu tượng cho mọi loại việc trong lịch trình 1 ngày.
 * <p>
 * Đây là chỗ thể hiện rõ nhất 3 trụ cột OOP của đồ án:
 * <ul>
 *   <li><b>Trừu tượng (Abstraction)</b>: {@link #coTheDoiLich()} và
 *       {@link #xuLyKhiBiTre(int)} không có thân hàm ở đây, buộc mọi lớp con
 *       phải tự định nghĩa "phản ứng" riêng của mình.</li>
 *   <li><b>Đa hình (Polymorphism)</b>: {@code ReplanEngine} chỉ cần gọi
 *       {@code item.xuLyKhiBiTre(soPhut)} mà không cần biết item là loại gì —
 *       hành vi thực thi khác nhau tùy vào lớp con thực tế (FixedEvent,
 *       FlexibleTask, DeadlineTask, RecurringEvent).</li>
 *   <li><b>Đóng gói (Encapsulation) / Tell-Don't-Ask</b>: gioBatDau, gioKetThuc
 *       là private, chỉ có thể thay đổi qua {@link #doiLich(int)} — lớp gọi
 *       (ReplanEngine, Disruption) không hề "táy máy" chỉnh trực tiếp field.</li>
 * </ul>
 */
public abstract class ScheduleItem {

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
     * Constructor dùng khi SAO CHÉP 1 item đã có (xem
     * {@link com.oopsmyschedule.service.DaySchedule#saoChep()}). Giữ nguyên
     * {@code id} và trạng thái hoàn thành gốc — bắt buộc phải giữ nguyên id
     * vì {@link SkipDisruption} và {@code ReplanEngine} cần đối chiếu "cùng 1
     * việc" giữa bản gốc (người dùng chọn trên UI) và bản sao độc lập mà
     * engine đang thao tác — nếu mỗi lần sao chép lại sinh id ngẫu nhiên mới,
     * việc bị chọn để "bỏ" sẽ không bao giờ khớp được với item nào trong danh
     * sách đang xử lý.
     */
    protected ScheduleItem(String id, String ten, LocalTime gioBatDau, LocalTime gioKetThuc,
                            PriorityLevel doUuTien, boolean hoanThanh) {
        // Guard clause: không cho tạo item có giờ bắt đầu >= giờ kết thúc.
        if (gioBatDau == null || gioKetThuc == null) {
            throw new IllegalArgumentException("Giờ bắt đầu / giờ kết thúc không được null.");
        }
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

    /**
     * Phản ứng khi bị trễ {@code soPhutTre} phút. Đây chính là chỗ đa hình
     * "kép" của đề tài: tùy loại ScheduleItem mà hành vi hoàn toàn khác nhau
     * (dời giờ, ném lỗi, kiểm tra hạn chót, hay bỏ qua hôm nay).
     */
    public abstract void xuLyKhiBiTre(int soPhutTre);

    /** Mô tả ngắn gọn lý do/kết quả xử lý gần nhất, dùng để hiển thị ở bảng so sánh. */
    public abstract String moTaTrangThai();

    // ---------- Helper được bảo vệ (protected) cho lớp con tái sử dụng ----------

    /**
     * Dời cả gioBatDau và gioKetThuc đi {@code soPhut} phút, giữ nguyên
     * khoảng thời lượng (duration). Dùng chung cho các lớp con "linh hoạt".
     */
    protected void doiLich(int soPhut) {
        kiemTraChuaHoanThanh("dời lịch"); // INV-18
        // INV-03: guard TRƯỚC khi đổi bất kỳ field nào, để object không bị đổi nửa chừng.
        if (vuotNuaDemNeuDoi(soPhut)) {
            throw new IllegalArgumentException(
                "Dời " + soPhut + " phút sẽ làm giờ kết thúc (" + gioKetThuc + ") vượt 23:59.");
        }
        this.gioBatDau = this.gioBatDau.plusMinutes(soPhut);
        this.gioKetThuc = this.gioKetThuc.plusMinutes(soPhut);
    }

    /** Mốc cuối ngày hợp lệ (23:59) tính bằng giây: 86340. */
    private static final long CUOI_NGAY_GIAY = LocalTime.of(23, 59).toSecondOfDay();

    /**
     * Dời {@code soPhut} phút có làm giờ kết thúc vượt 23:59 không? Tính bằng giây
     * tuyệt đối vì {@code LocalTime.plusMinutes} cuộn vòng qua nửa đêm. Kết thúc
     * đúng 23:59 vẫn hợp lệ. Chỉ đọc, không đổi trạng thái.
     */
    public boolean vuotNuaDemNeuDoi(int soPhut) {
        return gioKetThuc.toSecondOfDay() + soPhut * 60L > CUOI_NGAY_GIAY;
    }

    /**
     * Giữ lại từ main (K-14): dời {@code soPhut} phút có còn nằm trong ngày (không cuộn
     * vòng qua 00:00) không? Với soPhut >= 0 tương đương {@code !vuotNuaDemNeuDoi(soPhut)}.
     */
    public boolean doiDuocTrongNgay(int soPhut) {
        long batDau = gioBatDau.toSecondOfDay() + soPhut * 60L;
        long ketThuc = gioKetThuc.toSecondOfDay() + soPhut * 60L;
        return batDau >= 0 && ketThuc <= 24 * 3600L - 60;
    }

    /** INV-18: việc đã hoàn thành không bị dời / đặt lại giờ. */
    private void kiemTraChuaHoanThanh(String hanhDong) {
        if (hoanThanh) {
            throw new IllegalStateException(
                "Việc \"" + ten + "\" đã hoàn thành, không thể " + hanhDong + ".");
        }
    }

    /** Đặt lại khung giờ tuyệt đối (dùng khi ReplanEngine cần "ép" 1 vị trí mới cụ thể). */
    protected void datLaiKhungGio(LocalTime moiBatDau, LocalTime moiKetThuc) {
        kiemTraChuaHoanThanh("đặt lại khung giờ"); // INV-18
        if (!moiBatDau.isBefore(moiKetThuc)) {
            throw new IllegalArgumentException("Khung giờ mới không hợp lệ: " + moiBatDau + " - " + moiKetThuc);
        }
        if (!Duration.between(moiBatDau, moiKetThuc).equals(layThoiLuong())) { // INV-08
            throw new IllegalArgumentException("Không được thay đổi thời lượng của việc.");
        }
        this.gioBatDau = moiBatDau;
        this.gioKetThuc = moiKetThuc;
    }

    /**
     * Cho phép {@code ReplanEngine} (tầng điều phối) gán một khung giờ mới cụ
     * thể sau khi đã phân tích ưu tiên/xung đột toàn cục — khác với
     * {@link #xuLyKhiBiTre(int)} vốn là phản ứng "tự quyết" của riêng item.
     * Vẫn giữ nguyên guard clause (giờ bắt đầu phải trước giờ kết thúc) nên
     * không phá vỡ tính hợp lệ của dữ liệu dù được gọi từ ngoài.
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

    /** Đánh dấu việc đã hoàn thành — BoTheoDoiLich sẽ bỏ qua, không nhắc trễ nữa. */
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

    /**
     * Đồng nhất theo {@code id}, KHÔNG theo nội dung (giờ giấc/tên có thể bị
     * ReplanEngine chỉnh sửa liên tục). Bắt buộc phải có để
     * {@link java.util.List#remove(Object)} trong {@link SkipDisruption} và
     * mọi thao tác đối chiếu "cùng 1 việc" giữa bản gốc và bản sao (xem
     * {@link com.oopsmyschedule.service.DaySchedule#saoChep()}) hoạt động
     * đúng — nếu không override, Object.equals() mặc định so theo địa chỉ
     * tham chiếu, khiến 1 item và bản sao "giống hệt" của nó bị coi là 2
     * item khác nhau.
     */
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