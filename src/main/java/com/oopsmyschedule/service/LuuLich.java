package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.DeadlineTask;
import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.FlexibleTask;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.RecurringEvent;
import com.oopsmyschedule.domain.ScheduleItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lưu / đọc {@link DaySchedule} ra file JSON.
 * <p>
 * Đây KHÔNG phải một bộ JSON parser tổng quát — vì đồ án chỉ dùng JDK thuần
 * (không tải thêm dependency ngoài), lớp này tự viết ra đúng định dạng JSON
 * hợp lệ và tự đọc lại đúng định dạng đó bằng regex đơn giản. Nếu sau này
 * nhóm muốn dùng Gson/Jackson thật, chỉ cần thay nội dung 2 phương thức
 * {@link #luu} và {@link #doc}, phần còn lại của chương trình không đổi.
 * <p>
 * FIX BUG "Persistence chưa đủ an toàn": bản cũ chỉ lưu loai/ten/gio/doUuTien
 * (+ hanChot/streak) — nghĩa là sau khi tắt/mở lại app, các trạng thái sau
 * ĐỀU BỊ MẤT: {@code id} (khiến SkipDisruption không đối chiếu lại được),
 * {@code hoanThanh}, {@code soLanDaDoi} (FlexibleTask), {@code boQuaHomNay}
 * (RecurringEvent), {@code ngayThucHien}/{@code canhBaoTreHan}
 * (DeadlineTask), và ở cấp {@link DaySchedule}: {@code soDaHuy} +
 * {@code nhatKySuCo}. Giờ lưu đủ toàn bộ state để SAVE → LOAD là round-trip
 * trung thực (xem test VAL-19).
 * <p>
 * Ghi chú kỹ thuật: {@code danhSachThayDoi}/{@code tenKhongTheSapXep} của
 * mỗi {@link SuCoNhatKy} là danh sách chuỗi — thay vì tự dựng mảng JSON lồng
 * nhau (phức tạp không cần thiết cho 1 regex parser tự chế), ta nối chúng
 * lại bằng dấu phân cách hiếm gặp {@link #NGAN_CACH} thành 1 chuỗi duy nhất.
 */
public final class LuuLich {

    private LuuLich() {
    }

    /** Dấu phân cách dùng để nối nhiều dòng của 1 danh sách chuỗi thành 1 field JSON string. */
    private static final String NGAN_CACH = "~~|~~";

    public static void luu(DaySchedule ds, Path duongDan) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"ngay\": \"").append(ds.getNgay()).append("\",\n");
        sb.append("  \"soDaHuy\": ").append(ds.getSoDaHuy()).append(",\n");
        sb.append("  \"cacViec\": [\n");
        ghiDanhSachItem(sb, ds.layLichHienTai());
        sb.append("  ],\n");

        sb.append("  \"chuaXepLich\": [\n");
        ghiDanhSachItem(sb, ds.getChuaXepLich());
        sb.append("  ],\n");
        sb.append("  \"nhatKySuCo\": [\n");
        List<SuCoNhatKy> nk = ds.getNhatKySuCo();
        for (int i = 0; i < nk.size(); i++) {
            SuCoNhatKy n = nk.get(i);
            sb.append("    {");
            sb.append("\"gio\": \"").append(n.getGio()).append("\", ");
            sb.append("\"moTaSuCo\": \"").append(escape(n.getMoTaSuCo())).append("\", ");
            sb.append("\"tenPhuongAn\": \"").append(escape(n.getTenPhuongAn())).append("\", ");
            sb.append("\"danhSachThayDoi\": \"").append(escape(noiChuoi(n.getDanhSachThayDoiThucSu()))).append("\", ");
            sb.append("\"tenKhongTheSapXep\": \"").append(escape(noiChuoi(n.getTenKhongTheSapXep()))).append("\"");
            sb.append("}");
            sb.append(i < nk.size() - 1 ? ",\n" : "\n");
        }
        sb.append("  ]\n}\n");
        // FIX (audit bản 12, mục 2): trước đây ghi thẳng vào duongDan bằng
        // Files.writeString — nếu app bị tắt/crash đúng lúc đang ghi thì file
        // có thể dở dang, hỏng dữ liệu cũ. Nay ghi ra file .tmp trước, ghi
        // xong mới REPLACE nguyên tử (ATOMIC_MOVE) vào file thật — nếu quá
        // trình bị ngắt giữa chừng thì file .tmp hỏng chứ file thật (cũ) vẫn
        // nguyên vẹn.
        Path fileTam = duongDan.resolveSibling(duongDan.getFileName() + ".tmp");
        Files.writeString(fileTam, sb.toString(), StandardCharsets.UTF_8);
        try {
            Files.move(fileTam, duongDan,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
            // Một số hệ thống file (VD: ổ đĩa khác) không hỗ trợ atomic move
            // xuyên thiết bị -> fallback sang move thường (vẫn tốt hơn ghi
            // thẳng, chỉ mất tính atomic tuyệt đối).
            Files.move(fileTam, duongDan, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }


    private static String noiChuoi(List<String> danhSach) {
        return danhSach == null ? "" : String.join(NGAN_CACH, danhSach);
    }

    private static void ghiDanhSachItem(
            StringBuilder sb,
            List<ScheduleItem> danhSach) {

        for (int i = 0; i < danhSach.size(); i++) {
            ScheduleItem it = danhSach.get(i);

            sb.append("    {");
            sb.append("\"id\": \"").append(escape(it.getId())).append("\", ");
            sb.append("\"loai\": \"").append(escape(it.loaiViec())).append("\", ");
            sb.append("\"ten\": \"").append(escape(it.getTen())).append("\", ");
            sb.append("\"gioBatDau\": \"").append(it.getGioBatDau()).append("\", ");
            sb.append("\"gioKetThuc\": \"").append(it.getGioKetThuc()).append("\", ");
            sb.append("\"doUuTien\": \"").append(it.getDoUuTien().name()).append("\", ");
            sb.append("\"hoanThanh\": ").append(it.isHoanThanh());

            if (it instanceof DeadlineTask dt) {
                sb.append(", \"hanChot\": \"").append(dt.getHanChot()).append("\"");
                sb.append(", \"ngayThucHien\": \"").append(dt.getNgayThucHien()).append("\"");
                sb.append(", \"canhBaoTreHan\": ").append(dt.isCanhBaoTreHan());
            }

            if (it instanceof RecurringEvent re) {
                sb.append(", \"streak\": ").append(re.getSoNgayLienTiep());
                sb.append(", \"boQuaHomNay\": ").append(re.isBoQuaHomNay());
            }

            if (it instanceof FlexibleTask ft) {
                sb.append(", \"soLanDaDoi\": ").append(ft.getSoLanDaDoi());
            }

            sb.append("}");
            sb.append(i < danhSach.size() - 1 ? ",\n" : "\n");
        }
    }

    private static List<String> tachChuoi(String s) {
        if (s == null || s.isEmpty()) {
            return List.of();
        }
        return List.of(s.split(Pattern.quote(NGAN_CACH)));
    }

    // FIX BUG persistence (audit bản 10, mục 2): field "ten" là chuỗi TỰ DO
    // do người dùng gõ, có thể chứa dấu " hoặc \ (đã được escape() ở luu()).
    // Regex cũ dùng "([^\"]*)" — lớp ký tự "không phải dấu \"" — không hề
    // hiểu khái niệm escape: gặp CHUỖI \" (backslash rồi mới tới ") thì ký
    // tự " đó vẫn khớp là dấu đóng chuỗi, cắt cụt nội dung ngay tại đó (vd
    // tên A "quote" and \ slash sau SAVE→LOAD bị đọc hụt thành "A \"). Đổi
    // sang CHUOI_CO_ESCAPE: hoặc 1 ký tự không phải " và không phải \, hoặc
    // 1 cặp \X bất kỳ (escaped-pair) — đúng chuẩn cách 1 regex tự chế đọc
    // lại 1 JSON string đã escape.
    private static final String CHUOI_CO_ESCAPE = "((?:[^\"\\\\]|\\\\.)*)";
    private static final Pattern MAU_ITEM = Pattern.compile(
        "\\{[^{}]*?\"id\"\\s*:\\s*\"" + CHUOI_CO_ESCAPE + "\"[^{}]*?\"loai\"\\s*:\\s*\"([^\"]*)\"[^{}]*?" +
        "\"ten\"\\s*:\\s*\"" + CHUOI_CO_ESCAPE + "\"[^{}]*?\"gioBatDau\"\\s*:\\s*\"([^\"]*)\"[^{}]*?" +
        "\"gioKetThuc\"\\s*:\\s*\"([^\"]*)\"[^{}]*?\"doUuTien\"\\s*:\\s*\"([^\"]*)\"[^{}]*?" +
        "\"hoanThanh\"\\s*:\\s*(true|false)([^{}]*)\\}");
    private static final Pattern MAU_HAN_CHOT = Pattern.compile("\"hanChot\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern MAU_NGAY_THUC_HIEN = Pattern.compile("\"ngayThucHien\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern MAU_CANH_BAO = Pattern.compile("\"canhBaoTreHan\"\\s*:\\s*(true|false)");
    private static final Pattern MAU_STREAK = Pattern.compile("\"streak\"\\s*:\\s*(\\d+)");
    private static final Pattern MAU_BO_QUA = Pattern.compile("\"boQuaHomNay\"\\s*:\\s*(true|false)");
    private static final Pattern MAU_SO_LAN_DOI = Pattern.compile("\"soLanDaDoi\"\\s*:\\s*(\\d+)");
    private static final Pattern MAU_NGAY = Pattern.compile("\"ngay\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern MAU_SO_DA_HUY = Pattern.compile("\"soDaHuy\"\\s*:\\s*(\\d+)");
    private static final Pattern MAU_NHAT_KY = Pattern.compile(
        "\\{[^{}]*?\"gio\"\\s*:\\s*\"([^\"]*)\"[^{}]*?\"moTaSuCo\"\\s*:\\s*\"" + CHUOI_CO_ESCAPE + "\"[^{}]*?" +
        "\"tenPhuongAn\"\\s*:\\s*\"" + CHUOI_CO_ESCAPE + "\"[^{}]*?\"danhSachThayDoi\"\\s*:\\s*\"" + CHUOI_CO_ESCAPE + "\"[^{}]*?" +
        "\"tenKhongTheSapXep\"\\s*:\\s*\"" + CHUOI_CO_ESCAPE + "\"[^{}]*?\\}");
    private static final Pattern MAU_CAC_VIEC = Pattern.compile(
            "\"cacViec\"\\s*:\\s*\\[(.*?)\\]",
            Pattern.DOTALL);

    private static final Pattern MAU_CHUA_XEP_LICH = Pattern.compile(
            "\"chuaXepLich\"\\s*:\\s*\\[(.*?)\\]",
            Pattern.DOTALL);
    /**
     * FIX BUG (audit bản 10, mục 3 — phần còn sót): vì {@link #doc} KHÔNG
     * phải parser JSON thật, nó có thể "im lặng" bỏ qua nội dung không
     * khớp regex (vd mảng "cacViec" bị cắt cụt giữa chừng) rồi trả về y
     * hệt 1 lịch hợp lệ nhưng rỗng — khiến file hỏng bị hiểu nhầm thành
     * "lịch trống" thay vì ném lỗi để {@link KhoDuLieu} phát hiện. Đây là
     * kiểm tra thô (không thay thế 1 JSON validator thật) nhưng bắt được
     * phần lớn kiểu hỏng thường gặp: thiếu dấu đóng ngoặc (ghi dở/copy
     * lỗi) hoặc thiếu hẳn 2 trường bắt buộc.
     */
    private static void kiemTraCauTrucHopLe(String noiDung, Path duongDan) throws IOException {
        long moNgoac = noiDung.chars().filter(c -> c == '{').count();
        long dongNgoac = noiDung.chars().filter(c -> c == '}').count();
        long moMang = noiDung.chars().filter(c -> c == '[').count();
        long dongMang = noiDung.chars().filter(c -> c == ']').count();
        boolean canBang = moNgoac == dongNgoac && moNgoac > 0 && moMang == dongMang && moMang > 0;
        boolean coDuKhoa = noiDung.contains("\"ngay\"") && noiDung.contains("\"cacViec\"");
        if (!canBang || !coDuKhoa) {
            throw new IOException("File JSON không đúng định dạng (" + duongDan.getFileName()
                + ") — thiếu dấu đóng ngoặc hoặc thiếu trường bắt buộc.");
        }
    }

    public static DaySchedule doc(Path duongDan) throws IOException {
        String noiDung = Files.readString(duongDan, StandardCharsets.UTF_8);
        kiemTraCauTrucHopLe(noiDung, duongDan);

        LocalDate ngay = LocalDate.now();
        Matcher mNgay = MAU_NGAY.matcher(noiDung);
        if (mNgay.find()) {
            ngay = LocalDate.parse(mNgay.group(1));
        }

        DaySchedule ds = new DaySchedule(ngay);
        Matcher mCacViec = MAU_CAC_VIEC.matcher(noiDung);
        if (mCacViec.find()) {
            List<ScheduleItem> cacViec = docDanhSachItem(mCacViec.group(1), ngay);
            for (ScheduleItem item : cacViec) {
                ds.themItem(item);
            }
        }

        Matcher mChuaXep = MAU_CHUA_XEP_LICH.matcher(noiDung);
        if (mChuaXep.find()) {
            List<ScheduleItem> chuaXep = docDanhSachItem(mChuaXep.group(1), ngay);
            for (ScheduleItem item : chuaXep) {
                ds.themChuaXepLich(item);
            }
        }

        int soDaHuy = 0;
        Matcher mHuy = MAU_SO_DA_HUY.matcher(noiDung);
        if (mHuy.find()) {
            soDaHuy = Integer.parseInt(mHuy.group(1));
        }
        List<SuCoNhatKy> nhatKySuCo = new ArrayList<>();
        Matcher mNk = MAU_NHAT_KY.matcher(noiDung);
        while (mNk.find()) {
            LocalTime gio = LocalTime.parse(mNk.group(1));
            String moTaSuCo = unescape(mNk.group(2));
            String tenPhuongAn = unescape(mNk.group(3));
            List<String> danhSachThayDoi = tachChuoi(unescape(mNk.group(4)));
            List<String> tenKhongTheSapXep = tachChuoi(unescape(mNk.group(5)));
            nhatKySuCo.add(new SuCoNhatKy(gio, moTaSuCo, tenPhuongAn, danhSachThayDoi, tenKhongTheSapXep));
        }
        ds.khoiPhucTuFile(soDaHuy, nhatKySuCo);

        return ds;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }


    private static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static List<ScheduleItem> docDanhSachItem(
            String noiDung,
            LocalDate ngay) {

        List<ScheduleItem> danhSach = new ArrayList<>();
        Matcher m = MAU_ITEM.matcher(noiDung);

        while (m.find()) {
            String id = unescape(m.group(1));
            String loai = m.group(2);
            String ten = unescape(m.group(3));
            LocalTime bd = LocalTime.parse(m.group(4));
            LocalTime kt = LocalTime.parse(m.group(5));
            PriorityLevel uuTien = PriorityLevel.valueOf(m.group(6));
            boolean hoanThanh = Boolean.parseBoolean(m.group(7));
            String phanConLai = m.group(8);

            ScheduleItem item = switch (loai) {
                case "Cố định" ->
                        new FixedEvent(id, ten, bd, kt, uuTien, hoanThanh);

                case "Deadline" -> {
                    Matcher mh = MAU_HAN_CHOT.matcher(phanConLai);
                    LocalDateTime hanChot = mh.find()
                            ? LocalDateTime.parse(mh.group(1))
                            : LocalDateTime.of(ngay, LocalTime.of(23, 59));

                    Matcher mn = MAU_NGAY_THUC_HIEN.matcher(phanConLai);
                    LocalDate ngayThucHien = mn.find()
                            ? LocalDate.parse(mn.group(1)) : ngay;

                    Matcher mc = MAU_CANH_BAO.matcher(phanConLai);
                    boolean canhBao = mc.find()
                            && Boolean.parseBoolean(mc.group(1));

                    yield new DeadlineTask(
                            id, ten, bd, kt, uuTien, hoanThanh,
                            hanChot, ngayThucHien, canhBao);
                }

                case "Lặp lại" -> {
                    Matcher ms = MAU_STREAK.matcher(phanConLai);
                    int streak = ms.find()
                            ? Integer.parseInt(ms.group(1)) : 0;

                    Matcher mb = MAU_BO_QUA.matcher(phanConLai);
                    boolean boQua = mb.find()
                            && Boolean.parseBoolean(mb.group(1));

                    yield new RecurringEvent(
                            id, ten, bd, kt, uuTien, hoanThanh,
                            streak, boQua);
                }

                default -> {
                    Matcher msl = MAU_SO_LAN_DOI.matcher(phanConLai);
                    int soLanDaDoi = msl.find()
                            ? Integer.parseInt(msl.group(1)) : 0;

                    yield new FlexibleTask(
                            id, ten, bd, kt, uuTien, hoanThanh,
                            soLanDaDoi);
                }
            };

            danhSach.add(item);
        }

        return danhSach;
    }
}
