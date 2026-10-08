package com.oopsmyschedule.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;

/**
 * Đồng hồ của ứng dụng — có 2 chế độ:
 * <ul>
 *   <li><b>Normal Mode</b>: lấy giờ thật của máy ({@link LocalTime#now()}).</li>
 *   <li><b>Simulation Mode</b>: bắt đầu từ 1 mốc giờ giả lập (tua nhanh bằng
 *       nút +30/+60/+90 phút để demo cảnh "vỡ lịch" mà không cần chờ tới
 *       đúng giờ thật) — NHƯNG SAU MỐC ĐÓ VẪN TRÔI TIẾP THEO THỜI GIAN THỰC,
 *       không đứng yên một chỗ.</li>
 * </ul>
 * <p>
 * Trước đây bug ở chỗ: Simulation Mode dùng 1 {@code LocalTime} tĩnh, chỉ đổi
 * khi bấm nút tua — nghĩa là sau khi bật Demo Mode, đồng hồ "đứng luôn" mãi
 * cho tới lần bấm tua tiếp theo, dù {@code Timer} 1 giây của giao diện vẫn
 * chạy đều. Giờ lưu lại MỐC THỜI GIAN THẬT ({@code #mocThucDat}) tại lúc đặt
 * giờ giả lập, rồi mỗi lần gọi {@link #layGioHienTai()} sẽ cộng thêm đúng
 * khoảng thời gian thật đã trôi qua kể từ mốc đó — nên đồng hồ vẫn chạy tiếp
 * bình thường, chỉ là có 1 độ lệch (offset) so với giờ thật.
 */
public class DongHo {

    private boolean cheDoMoPhong = false;
    private LocalTime gioMoPhongGoc = LocalTime.of(7, 0);
    private Instant mocThucDat = Instant.now();

    /** Giờ hiện tại theo đúng chế độ đang bật — Simulation Mode vẫn TRÔI theo thời gian thực, không đứng yên. */
    public LocalTime layGioHienTai() {
        if (!cheDoMoPhong) {
            return LocalTime.now();
        }
        Duration daTroiQua = Duration.between(mocThucDat, Instant.now());
        return gioMoPhongGoc.plus(daTroiQua);
    }

    public boolean isCheDoMoPhong() {
        return cheDoMoPhong;
    }

    /** Bật chế độ mô phỏng, bắt đầu từ đúng giờ thật hiện tại (rồi vẫn trôi tiếp bình thường từ đó). */
    public void batCheDoMoPhong() {
        datMocMoPhong(LocalTime.now());
        this.cheDoMoPhong = true;
    }

    public void tatCheDoMoPhong() {
        this.cheDoMoPhong = false;
    }

    /** Tua nhanh giờ giả lập thêm {@code soPhut} phút (dùng cho nút "⏩ +30 phút"...), rồi tiếp tục trôi từ mốc mới. */
    public void tuaNhanh(int soPhut) {
        LocalTime hienTai = cheDoMoPhong ? layGioHienTai() : LocalTime.now();
        datMocMoPhong(hienTai.plusMinutes(soPhut));
        this.cheDoMoPhong = true;
    }

    public void datGioMoPhong(LocalTime gio) {
        datMocMoPhong(gio);
        this.cheDoMoPhong = true;
    }

    /** Ghi lại "mốc" giờ giả lập + đúng thời điểm thật tương ứng, để layGioHienTai() tính tiếp phần trôi qua. */
    private void datMocMoPhong(LocalTime goc) {
        this.gioMoPhongGoc = goc;
        this.mocThucDat = Instant.now();
    }
}
