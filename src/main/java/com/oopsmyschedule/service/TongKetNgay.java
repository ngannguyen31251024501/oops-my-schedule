package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.FlexibleTask;
import com.oopsmyschedule.domain.RecurringEvent;
import com.oopsmyschedule.domain.ScheduleItem;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * "Tổng kết ngày" — biến app từ "lập lịch + cứu lịch" thành "lập → thực hiện
 * → đánh giá → rút kinh nghiệm". Cố tình làm kiểu Daily Summary đơn giản
 * (không phải dashboard thống kê khổng lồ, không cần AI): chỉ 2 chỉ số hoàn
 * thành + vài dòng lời khuyên rule-based, dễ giải thích với giảng viên.
 * <p>
 * QUAN TRỌNG: không chỉ tính "X/Y việc = Z%" theo SỐ LƯỢNG, vì 1 việc 4 tiếng
 * và 1 việc 15 phút không nên có "trọng số" ngang nhau — nên có thêm chỉ số
 * thứ hai theo THỜI LƯỢNG ({@link #getPhanTramKhoiLuong()}).
 */
public class TongKetNgay {

    private final int tongSoViec;
    private final int soHoanThanh;
    private final int soTre;
    private final int soDaDoi;
    private final int soDaHuy;
    private final long tongPhutKhoiLuong;
    private final long phutDaHoanThanh;
    private final List<String> loiKhuyen;
    private final List<SuCoNhatKy> nhatKySuCo;

    private TongKetNgay(int tongSoViec, int soHoanThanh, int soTre, int soDaDoi, int soDaHuy,
                         long tongPhutKhoiLuong, long phutDaHoanThanh, List<String> loiKhuyen,
                         List<SuCoNhatKy> nhatKySuCo) {
        this.tongSoViec = tongSoViec;
        this.soHoanThanh = soHoanThanh;
        this.soTre = soTre;
        this.soDaDoi = soDaDoi;
        this.soDaHuy = soDaHuy;
        this.tongPhutKhoiLuong = tongPhutKhoiLuong;
        this.phutDaHoanThanh = phutDaHoanThanh;
        this.loiKhuyen = loiKhuyen;
        this.nhatKySuCo = nhatKySuCo;
    }

    /**
     * Tính tổng kết cho 1 {@link DaySchedule} tại thời điểm {@code hienTai}.
     * "Trễ" = việc chưa hoàn thành mà giờ bắt đầu đã qua thời điểm hiện tại
     * (tận dụng lại đúng định nghĩa mà {@link BoTheoDoiLich} đang dùng).
     * "Đã dời" = FlexibleTask từng bị dời ít nhất 1 lần, hoặc RecurringEvent
     * đã bỏ qua hôm nay (streak reset) — cả 2 đều là "lịch không diễn ra như
     * kế hoạch ban đầu" nhưng KHÔNG bị người dùng chủ động xóa hẳn.
     * "Đã hủy" = số lần người dùng bấm 🗑️ Xóa/Hủy trong ngày ({@link DaySchedule#getSoDaHuy()}).
     */
    public static TongKetNgay tinhToan(DaySchedule ds, LocalTime hienTai) {
        List<ScheduleItem> ds1 = ds.layLichHienTai();
        int daHuy = ds.getSoDaHuy();
        // FIX BUG "Daily Summary bỏ việc đã hủy khỏi mẫu số" (audit, mục 7):
        // bản cũ chỉ tính tong = ds1.size() — vì huyItem() đã XÓA hẳn item
        // khỏi danh sách, 1 ngày có 10 việc, hủy 2, hoàn thành 8/8 còn lại
        // sẽ hiện "100%" dù trực giác người dùng là "chỉ xong 8/10". Việc đã
        // hủy vẫn phải nằm trong "khối lượng công việc đã lên kế hoạch" của
        // ngày — chỉ không tính là "hoàn thành".
        int tong = ds1.size() + daHuy;
        int hoanThanh = 0;
        int tre = 0;
        int daDoi = 0;
        long tongPhut = 0;
        long phutXong = 0;

        for (ScheduleItem item : ds1) {
            long phut = item.layThoiLuong().toMinutes();
            tongPhut += phut;
            if (item.isHoanThanh()) {
                hoanThanh++;
                phutXong += phut;
                continue;
            }
            if (item.getGioBatDau().isBefore(hienTai)) {
                tre++;
            }
            if (item instanceof FlexibleTask ft && ft.getSoLanDaDoi() > 0) {
                daDoi++;
            } else if (item instanceof RecurringEvent re && re.isBoQuaHomNay()) {
                daDoi++;
            }
        }

        List<String> loiKhuyen = sinhLoiKhuyen(tong, hoanThanh, tre, daDoi, daHuy, tongPhut, phutXong);
        return new TongKetNgay(tong, hoanThanh, tre, daDoi, daHuy, tongPhut, phutXong, loiKhuyen, ds.getNhatKySuCo());
    }

    private static List<String> sinhLoiKhuyen(int tong, int hoanThanh, int tre, int daDoi, int daHuy,
                                                long tongPhut, long phutXong) {
        List<String> ds = new ArrayList<>();
        if (tong == 0) {
            ds.add("Chưa có hoạt động nào trong lịch hôm nay để tổng kết.");
            return ds;
        }
        double tiLeSoLuong = 100.0 * hoanThanh / tong;
        double tiLeKhoiLuong = tongPhut == 0 ? 100.0 : 100.0 * phutXong / tongPhut;

        if (tiLeSoLuong >= 90) {
            ds.add("🎉 Một ngày rất hiệu quả! Bạn đã hoàn thành gần như toàn bộ kế hoạch.");
        } else if (tiLeSoLuong < 60 && tre >= 2) {
            ds.add("😮‍💨 Lịch hôm nay có vẻ hơi dày. Ngày mai nên để trống thêm khoảng 30 phút giữa các hoạt động.");
        }
        if (daDoi >= 3) {
            ds.add("🔁 Bạn thường xuyên phải dời lịch hôm nay. Hãy thử giảm số hoạt động cố định trong 1 ngày.");
        }
        if (daHuy >= 2) {
            ds.add("✂️ Một số hoạt động chưa phù hợp với lịch hiện tại. Hãy cân nhắc giảm tải hoặc ưu tiên lại.");
        }
        if (tiLeKhoiLuong >= 90) {
            ds.add("💪 Bạn đã xử lý gần như toàn bộ khối lượng công việc hôm nay.");
        } else if (tiLeKhoiLuong < 60) {
            ds.add("📉 Khối lượng hoàn thành hôm nay khá thấp. Hãy xem lại các hoạt động chưa hoàn thành.");
        }
        if (ds.isEmpty()) {
            ds.add("Một ngày bình thường — không có gì bất thường để lưu ý.");
        }
        return ds;
    }

    public int getTongSoViec() {
        return tongSoViec;
    }

    public int getSoHoanThanh() {
        return soHoanThanh;
    }

    public int getSoTre() {
        return soTre;
    }

    public int getSoDaDoi() {
        return soDaDoi;
    }

    public int getSoDaHuy() {
        return soDaHuy;
    }

    /** % hoàn thành theo SỐ LƯỢNG việc (0-100), 0 nếu chưa có việc nào. */
    public double getPhanTramSoLuong() {
        return tongSoViec == 0 ? 0 : 100.0 * soHoanThanh / tongSoViec;
    }

    /** % hoàn thành theo THỜI LƯỢNG (0-100), 0 nếu tổng khối lượng bằng 0. */
    public double getPhanTramKhoiLuong() {
        return tongPhutKhoiLuong == 0 ? 0 : 100.0 * phutDaHoanThanh / tongPhutKhoiLuong;
    }

    public long getTongPhutKhoiLuong() {
        return tongPhutKhoiLuong;
    }

    public long getPhutDaHoanThanh() {
        return phutDaHoanThanh;
    }

    public List<String> getLoiKhuyen() {
        return loiKhuyen;
    }

    /** Nhật ký sự cố trong ngày — nguồn dữ liệu cho mục "🔎 Vì sao hôm nay chưa đạt". */
    public List<SuCoNhatKy> getNhatKySuCo() {
        return nhatKySuCo;
    }

    /**
     * 1 câu "nguyên nhân chính" rule-based (không AI), suy ra từ chính số lần
     * phải điều chỉnh lịch trong ngày — đúng tinh thần tài liệu thiết kế:
     * "Lịch buổi sáng bị dồn quá nhiều hoạt động liên tiếp" kiểu vậy, nhưng
     * generic hơn vì không phân tích khung giờ cụ thể.
     */
    public String nguyenNhanChinh() {
        int soSuCo = nhatKySuCo.size();
        if (soSuCo == 0) {
            return null;
        }
        int soKhongTheSapXep = (int) nhatKySuCo.stream().mapToLong(n -> n.getTenKhongTheSapXep().size()).sum();
        if (soSuCo >= 3) {
            return "Hôm nay có tới " + soSuCo + " sự cố liên tiếp — lịch đang xếp khá sát giờ, "
                + "nên chừa thêm khoảng trống giữa các hoạt động để 1 sự cố không kéo theo dây chuyền.";
        }
        if (soKhongTheSapXep > 0) {
            return "Có hoạt động bị dồn tới mức cả hôm nay lẫn ngày mai đều không còn chỗ — nên giảm bớt số việc "
                + "\"Nên làm\" trong 1-2 ngày liên tiếp.";
        }
        return "Lịch bị ảnh hưởng bởi " + soSuCo + " sự cố phát sinh trong ngày, nhưng Replan Engine đã sắp xếp lại ổn thỏa.";
    }

    /**
     * "6 giờ 20 phút / 8 giờ" — cố tình KHÔNG import gói {@code ui} từ đây
     * (service không nên phụ thuộc ngược vào ui), nên tự định dạng lại 1 hàm
     * nhỏ tương đương {@code ui.DinhDang.phut} thay vì gọi chéo package.
     */
    public String moTaKhoiLuong() {
        return dinhDangPhut(phutDaHoanThanh) + " / " + dinhDangPhut(tongPhutKhoiLuong);
    }

    private static String dinhDangPhut(long soPhut) {
        if (soPhut < 60) {
            return soPhut + " phút";
        }
        long gio = soPhut / 60;
        long conLai = soPhut % 60;
        return conLai == 0 ? gio + " giờ" : gio + " giờ " + conLai + " phút";
    }
}
