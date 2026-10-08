package com.oopsmyschedule.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Quản lý việc lưu/đọc dữ liệu lịch tự động vào thư mục dữ liệu riêng của
 * ứng dụng — KHÔNG bắt người dùng tự chọn nơi lưu bằng {@code JFileChooser}
 * nữa (xem thảo luận thiết kế: app quản lý lịch nên tự quản lý dữ liệu,
 * giống app thật, không phải "database lộ ra ngoài cho người dùng tự sờ").
 * <p>
 * Trên Windows: {@code %APPDATA%\OhMySchedule\lich_<ngay>.json}.<br>
 * Nơi khác (hoặc không có biến {@code APPDATA}): {@code ~/.oopsmyschedule/lich_<ngay>.json}.
 * <p>
 * Mỗi ngày 1 file riêng (giữ đúng định dạng JSON mà {@link LuuLich} đã viết
 * — không đổi format, chỉ đổi NƠI lưu và cách gọi tự động).
 */
public final class KhoDuLieu {

    private KhoDuLieu() {
    }

    /** Thư mục dữ liệu của app, tự tạo nếu chưa có. */
    public static Path thuMucDuLieu() {
        String appData = System.getenv("APPDATA");
        Path thuMuc = (appData != null && !appData.isBlank())
            ? Path.of(appData, "OhMySchedule")
            : Path.of(System.getProperty("user.home"), ".oopsmyschedule");
        try {
            Files.createDirectories(thuMuc);
        } catch (IOException ignored) {
            // Nếu không tạo được thư mục, luu()/doc() bên dưới sẽ tự ném lỗi rõ ràng hơn.
        }
        return thuMuc;
    }

    public static Path duongDanChoNgay(LocalDate ngay) {
        return thuMucDuLieu().resolve("lich_" + ngay + ".json");
    }

    public static void luuNgay(DaySchedule ds) throws IOException {
        LuuLich.luu(ds, duongDanChoNgay(ds.getNgay()));
    }

    /**
     * Ném ra khi file dữ liệu của 1 ngày TỒN TẠI nhưng không đọc được (JSON
     * hỏng / không đúng định dạng) — FIX BUG (audit bản 10, mục 3): trước
     * đây {@link #docNeuCo} bắt {@link IOException} rồi trả về {@code null}
     * y hệt như trường hợp "chưa từng lưu", khiến file lỗi bị hiểu nhầm
     * thành "chưa có dữ liệu" và {@code Main}/{@code MainFrame} âm thầm tạo
     * đè 1 lịch mẫu mới lên trên — có nguy cơ MẤT DỮ LIỆU của người dùng mà
     * không ai biết. Giờ 2 trường hợp được phân biệt rõ: "file không tồn
     * tại" vẫn trả về {@code null} (được phép tạo lịch mới); "file tồn tại
     * nhưng lỗi" ném ngoại lệ riêng để tầng gọi phải chủ động báo cho người
     * dùng biết, thay vì âm thầm ghi đè.
     */
    public static final class DuLieuHongException extends IOException {
        public DuLieuHongException(Path duongDan, Throwable nguyenNhan) {
            super("Không thể đọc dữ liệu đã lưu (" + duongDan.getFileName()
                + ") — file có thể đã bị hỏng.", nguyenNhan);
        }
    }

    /**
     * Đọc dữ liệu đã lưu của 1 ngày.
     *
     * @return {@code null} nếu ngày đó CHƯA TỪNG được lưu (file không tồn tại) — gọi phía trên có thể tạo lịch mới an toàn.
     * @throws DuLieuHongException nếu file đã tồn tại nhưng đọc/parse thất bại — KHÔNG được tự ý coi là "lịch rỗng" rồi ghi đè.
     */
    public static DaySchedule docNeuCo(LocalDate ngay) throws DuLieuHongException {
        Path p = duongDanChoNgay(ngay);
        if (!Files.exists(p)) return null;
        try {
            return LuuLich.doc(p);
        } catch (IOException | RuntimeException e) {
            // RuntimeException (vd NumberFormatException, DateTimeParseException,
            // IllegalArgumentException từ constructor domain) cũng tính là "file
            // hỏng" — parser tự chế bằng regex không đảm bảo chỉ ném IOException.
            throw new DuLieuHongException(p, e);
        }
    }
}
