package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.Disruption;
import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.RecurringEvent;
import com.oopsmyschedule.domain.ScheduleItem;
import com.oopsmyschedule.domain.SkipDisruption;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Khung xử lý dùng chung cho mọi {@link ReplanStrategy} cụ thể. Chứa thuật
 * toán quét + đặt lại khung giờ (vốn trước đây nằm thẳng trong
 * {@code ReplanEngine}) — mỗi chiến lược con chỉ cần override
 * {@link #coTheHySinh(ScheduleItem)} (và tuỳ chọn
 * {@link #tienXuLyTruocKhiQuet}) để tạo ra 1 khẩu vị đánh đổi khác nhau, mà
 * KHÔNG phải chép lại toàn bộ vòng lặp xử lý xung đột.
 */
public abstract class AbstractReplanStrategy implements ReplanStrategy {

    protected static final LocalTime CUOI_NGAY = LocalTime.of(23, 59);

    @Override
    public KetQuaSoSanh apDung(DaySchedule ds, Disruption disruption, LocalTime thoiDiemHienTai) {
        DaySchedule lichTruoc = ds.saoChep();
        DaySchedule lichSau = ds.saoChep();
        KetQuaSoSanh ketQua = new KetQuaSoSanh(lichTruoc, lichSau);
        // K-13: chụp giờ ban đầu theo id (LocalTime bất biến nên không bị sửa ngầm)
        java.util.Map<String, LocalTime[]> gioBanDau = new java.util.HashMap<>();
        for (ScheduleItem goc : lichTruoc.layLichHienTai()) {
            gioBanDau.put(goc.getId(), new LocalTime[] { goc.getGioBatDau(), goc.getGioKetThuc() });
        }

        List<ScheduleItem> toanBo = new ArrayList<>(lichSau.layLichHienTai());

        // Bước 0 — FIX BUG: "Bỏ việc" phải xử lý được cả việc ĐÃ BẮT ĐẦU
        // trong quá khứ (vd: đang trễ giờ tắm 32 phút, bấm "Bỏ việc Tắm rửa").
        // Trước đây bug ở chỗ: code tách "đã qua / còn lại" TRƯỚC rồi mới áp
        // dụng disruption lên phần "còn lại" — nên SkipDisruption không bao
        // giờ thấy được item đã rơi vào nhóm "đã qua", coi như không có tác
        // dụng gì. Giờ SkipDisruption được áp dụng trên TOÀN BỘ danh sách,
        // trước khi tách nhóm theo thời gian (vốn chỉ có ý nghĩa cho việc DỜI
        // giờ của DelayDisruption, không áp dụng cho việc BỎ hẳn).
        if (disruption instanceof SkipDisruption skip) {
            ScheduleItem bi = skip.getItemBiBo();
            boolean coTrongLich = bi != null && toanBo.contains(bi);
            disruption.apDung(toanBo);
            if (coTrongLich) {
                ketQua.themThayDoi(bi instanceof RecurringEvent
                    ? "\"" + bi.getTen() + "\": bỏ qua hôm nay theo yêu cầu của bạn (dù đã trễ giờ)."
                    : "\"" + bi.getTen() + "\": đã BỎ khỏi lịch hôm nay theo yêu cầu của bạn (dù việc đã trễ giờ).");
            }
        }

        List<ScheduleItem> daQua = new ArrayList<>();
        List<ScheduleItem> conLai = new ArrayList<>();
        for (ScheduleItem item : toanBo) {
            if (item.getGioBatDau().isBefore(thoiDiemHienTai)) {
                daQua.add(item);
            } else {
                conLai.add(item);
            }
        }

        // Bước 1: phản ứng "máy móc" cấp item. SkipDisruption đã xử lý xong ở
        // Bước 0 nên tới đây chỉ còn DelayDisruption cần áp dụng.
        List<ScheduleItem> daXong = new ArrayList<>();
        for (ScheduleItem it : conLai) {
            if (it.isHoanThanh()) daXong.add(it);
        }
        conLai.removeAll(daXong);                    // K-03: không dời việc đã xong
        if (!(disruption instanceof SkipDisruption)) {
            disruption.apDung(conLai);
        }
        conLai.addAll(daXong);                       // trả lại để chiếm khung giờ
        conLai.sort((a, b) -> a.getGioBatDau().compareTo(b.getGioBatDau()));

        // Bước 1.5: hook cho chiến lược muốn chủ động xử lý TRƯỚC khi quét
        // xung đột (vd: "Giảm tải" chủ động bỏ bớt việc phụ nếu lịch còn lại
        // rõ ràng quá dày, thay vì đợi "hết chỗ" mới xử lý như 2 chiến lược kia).
        conLai = tienXuLyTruocKhiQuet(conLai, thoiDiemHienTai, ketQua);

        // Bước 2: quét và giải quyết xung đột theo thứ tự ưu tiên.
        List<ScheduleItem> ketQuaCuoi = new ArrayList<>();
        LocalTime conTro = thoiDiemHienTai;

        for (int i = 0; i < conLai.size(); i++) {
            ScheduleItem item = conLai.get(i);

            if (item instanceof FixedEvent) {
                if (item.isHoanThanh()) {  // hai nhánh giống hệt nhau                 // K-03: giữ nguyên giờ, vẫn chiếm chỗ
                    conTro = maxGio(conTro, item.getGioKetThuc());
                    ketQuaCuoi.add(item);
                    continue;
                }
                conTro = maxGio(conTro, item.getGioKetThuc());
                ketQuaCuoi.add(item);
                continue;
            }

            LocalTime gioiHanKe = timGioBatDauFixedEventTiepTheo(conLai, i);
            Duration thoiLuong = item.layThoiLuong();
            LocalTime batDauMongMuon = maxGio(item.getGioBatDau(), conTro);

            // FIX BUG cross-midnight (BR-16): không được dùng thẳng
            // batDauMongMuon.plus(thoiLuong) rồi so isAfter(CUOI_NGAY) — nếu
            // batDauMongMuon + thoiLuong vượt quá 24h, LocalTime.plus() "cuộn
            // vòng" qua 00:00 khiến giờ kết thúc trông NHỎ HƠN giờ bắt đầu
            // (vd 23:30 + 90 phút -> hiện thành "01:00"), nên điều kiện
            // isAfter(CUOI_NGAY) sai lệch KHÔNG phát hiện được vụ vượt ngày,
            // và ganLaiKhungGioBoiEngine() có thể ném IllegalArgumentException
            // (giờ bắt đầu "sau" giờ kết thúc). Phải tính bằng tổng giây
            // tuyệt đối trước khi cộng.
            boolean vuotQuaNgay = batDauMongMuon.toSecondOfDay() + thoiLuong.getSeconds()
                > CUOI_NGAY.toSecondOfDay();
            LocalTime ketThucMongMuon = vuotQuaNgay ? CUOI_NGAY : batDauMongMuon.plus(thoiLuong);

            boolean khongDuCho = vuotQuaNgay
                || (gioiHanKe != null && ketThucMongMuon.isAfter(gioiHanKe));

            if (khongDuCho) {
                if (coTheHySinh(item)) {
                    ketQua.themDeXuatBo(item);
                    ketQua.themThayDoi("\"" + item.getTen() + "\": đề xuất BỎ vì không còn đủ thời gian trống.");
                    continue; // không chiếm chỗ, không cập nhật conTro
                }
                if (item instanceof RecurringEvent recurring) {
                    recurring.boQuaHomNay();
                    ketQua.themThayDoi("\"" + item.getTen() + "\": bỏ qua hôm nay (streak reset), không ảnh hưởng các ngày khác.");
                    ketQuaCuoi.add(item);
                    continue; // không cập nhật conTro vì đã bỏ qua
                }

                // FIX BUG cross-midnight (audit bản 10, mục 1): trước đây khi
                // vuotQuaNgay=true, code vẫn ép đặt item vào batDauMongMuon -
                // CUOI_NGAY (23:59), ÂM THẦM CẮT MẤT thời lượng thật của task
                // (vd task cần 60 phút chỉ còn 29 phút). Giờ KHÔNG ép/cắt
                // duration nữa: coi như "không đủ chỗ trong ngày hôm nay",
                // đưa vào cùng cơ chế "đề xuất" mà coTheHySinh dùng — nghĩa là
                // MainFrame sẽ thử chuyển việc này sang lịch NGÀY MAI (best
                // effort), hoặc báo "chưa thể sắp xếp" nếu ngày mai cũng kín.
                if (vuotQuaNgay) {
                    ketQua.themDeXuatBo(item);
                    ketQua.themThayDoi("\"" + item.getTen() + "\": không còn đủ " + thoiLuong.toMinutes()
                        + " phút trước nửa đêm — đề xuất chuyển sang ngày mai (không ép/cắt bớt thời lượng).");
                    continue; // không chiếm chỗ, không cập nhật conTro, không cắt duration
                }
                // NEN_LAM hoặc BAT_BUOC (hoặc DeadlineTask), không vượt ngày,
                // chỉ bị chặn bởi 1 FixedEvent kế tiếp -> vẫn ép đặt, kèm cảnh báo.
                item.ganLaiKhungGioBoiEngine(batDauMongMuon, ketThucMongMuon);
                dongBoCanhBaoDeadlineNeuCan(item);
                ketQua.themThayDoi("\"" + item.getTen() + "\": buộc phải dời tới " + batDauMongMuon
                    + " (không còn đủ chỗ lý tưởng).");
                ketQua.themCanhBao("\"" + item.getTen() + "\" (" + item.getDoUuTien()
                    + ") vẫn bị dồn ép tới " + batDauMongMuon + " — có thể ảnh hưởng việc sau hoặc giờ ngủ.");
                conTro = ketThucMongMuon;
                ketQuaCuoi.add(item);
                continue;
            }

            if (!batDauMongMuon.equals(item.getGioBatDau())) {
                item.ganLaiKhungGioBoiEngine(batDauMongMuon, ketThucMongMuon);
                dongBoCanhBaoDeadlineNeuCan(item);
                ketQua.themThayDoi("\"" + item.getTen() + "\": dời sang " + batDauMongMuon + " - " + ketThucMongMuon + ".");
            }
            conTro = ketThucMongMuon;
            ketQuaCuoi.add(item);
        }

        List<ScheduleItem> tongHop = new ArrayList<>(daQua);
        tongHop.addAll(ketQuaCuoi);
        lichSau.thayTheToanBo(tongHop);
        // K-13: đối chiếu giờ gốc với lịch kết quả theo id; chỉ bổ sung chỗ chưa có mô tả
        java.util.Set<String> idConLai = new java.util.HashSet<>();
        for (ScheduleItem sau : lichSau.layLichHienTai()) {
            idConLai.add(sau.getId());
            String ten = "\"" + sau.getTen() + "\"";
            LocalTime[] cu = gioBanDau.get(sau.getId());
            if (cu == null) {
                if (!daCoMoTa(ketQua, ten)) {
                    ketQua.themThayDoi(ten + ": được thêm vào lịch.");
                }
            } else if (!cu[0].equals(sau.getGioBatDau()) || !cu[1].equals(sau.getGioKetThuc())) {
                if (!daCoMoTa(ketQua, ten)) {
                    ketQua.themThayDoi(ten + ": dời từ " + cu[0] + "-" + cu[1]
                            + " sang " + sau.getGioBatDau() + "-" + sau.getGioKetThuc() + ".");
                }
            } // giờ không đổi thì không ghi gì
        }
        for (ScheduleItem truoc : lichTruoc.layLichHienTai()) {
            if (!idConLai.contains(truoc.getId())) {
                String ten = "\"" + truoc.getTen() + "\"";
                if (!daCoMoTa(ketQua, ten)) {
                    ketQua.themThayDoi(ten + ": không còn trong lịch hôm nay.");
                }
            }
        }

        if (ketQua.getDanhSachThayDoi().isEmpty()) {
            ketQua.themThayDoi("Không có việc nào cần dời — lịch vẫn ổn.");
        }

        return ketQua;
    }

    /**
     * Quyết định 1 việc "hy sinh được" hay không khi hết chỗ (đề xuất BỎ thay
     * vì ép dời/cảnh báo). Mặc định: chỉ những việc "Có thể bỏ". Đây chính là
     * điểm mỗi chiến lược con override khác nhau để tạo ra sự khác biệt thật
     * sự giữa 3 phương án — không chỉ khác tên gọi.
     */
    protected boolean coTheHySinh(ScheduleItem item) {
        return item.getDoUuTien() == PriorityLevel.CO_THE_BO;
    }

    /**
     * Hook tuỳ chọn: cho chiến lược con xử lý danh sách "còn lại" TRƯỚC khi
     * vào vòng quét xung đột chính. Mặc định không làm gì (giữ nguyên danh
     * sách nhận vào).
     */
    protected List<ScheduleItem> tienXuLyTruocKhiQuet(List<ScheduleItem> conLai, LocalTime thoiDiemHienTai,
                                                        KetQuaSoSanh ketQua) {
        return conLai;
    }

    /**
     * Gọi ngay sau mỗi lần {@code ganLaiKhungGioBoiEngine} ép đặt lại khung
     * giờ, để {@link com.oopsmyschedule.domain.DeadlineTask} tự cập nhật
     * đúng cờ cảnh báo trễ hạn theo giờ kết thúc MỚI (bug đã sửa — xem
     * javadoc của {@code dongBoCanhBaoTheoKhungGioHienTai}).
     */
    private static void dongBoCanhBaoDeadlineNeuCan(ScheduleItem item) {
        if (item instanceof com.oopsmyschedule.domain.DeadlineTask dt) {
            dt.dongBoCanhBaoTheoKhungGioHienTai();
        }
    }

    private static LocalTime maxGio(LocalTime a, LocalTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalTime timGioBatDauFixedEventTiepTheo(List<ScheduleItem> danhSach, int tuIndex) {
        for (int j = tuIndex + 1; j < danhSach.size(); j++) {
            if (danhSach.get(j) instanceof FixedEvent || danhSach.get(j).isHoanThanh()) {
                return danhSach.get(j).getGioBatDau();
            }
        }
        return null;
    }
    /** K-13: item đã có dòng mô tả (bắt đầu bằng "tên") thì không ghi thêm lần hai. */
    private static boolean daCoMoTa(KetQuaSoSanh ketQua, String tenCoNgoacKep) {
        for (String s : ketQua.getDanhSachThayDoi()) {
            if (s.startsWith(tenCoNgoacKep)) {
                return true;
            }
        }
        return false;
    }
}
