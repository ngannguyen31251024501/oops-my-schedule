package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.DelayDisruption;
import com.oopsmyschedule.domain.Disruption;
import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.RecurringEvent;
import com.oopsmyschedule.domain.ScheduleItem;
import com.oopsmyschedule.domain.SkipDisruption;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        // K-14: phân loại theo TRẠNG THÁI thật của từng việc, không chỉ theo giờ bắt đầu.
        //  - Việc đã xong -> giữ nguyên (không lập lịch lại).
        //  - Việc cố định đã kết thúc / đang diễn ra -> giữ nguyên; việc đang diễn ra
        //    còn CHẶN thời gian tới lúc nó kết thúc (conTro) nên không việc nào bị xếp đè lên.
        //  - Khi báo TRỄ: việc linh hoạt chưa xong mà đã tới/qua giờ = "lỡ giờ" -> được xếp lại
        //    (mốc sớm nhất = max(giờ cũ + số phút trễ, hiện tại)), không bị bỏ quên ở quá khứ.
        //  - Việc tương lai KHÔNG dời hàng loạt: chỉ bị đẩy khi thật sự va chạm (conTro).
        final boolean laTre = disruption instanceof DelayDisruption;
        final int soPhutTre = laTre ? ((DelayDisruption) disruption).getSoPhutTre() : 0;
        Map<String, LocalTime> moc = new HashMap<>();
        Map<String, LocalTime[]> gioBanDau = new HashMap<>();
        for (ScheduleItem it : toanBo) {
            gioBanDau.put(it.getId(), new LocalTime[]{it.getGioBatDau(), it.getGioKetThuc()});
        }

        List<ScheduleItem> daQua = new ArrayList<>();
        List<ScheduleItem> conLai = new ArrayList<>();
        LocalTime conTroBanDau = thoiDiemHienTai;
        for (ScheduleItem item : toanBo) {
            boolean daBatDau = item.getGioBatDau().isBefore(thoiDiemHienTai);
            if (item instanceof FixedEvent) {
                if (!item.getGioKetThuc().isAfter(thoiDiemHienTai)) {
                    daQua.add(item); // đã kết thúc
                } else if (daBatDau) {
                    daQua.add(item); // đang diễn ra: giữ nguyên + chặn thời gian tới lúc kết thúc
                    conTroBanDau = maxGio(conTroBanDau, item.getGioKetThuc());
                } else {
                    conLai.add(item);
                }
            } else if (item.isHoanThanh()) {
                daQua.add(item);
            } else if (laTre && !item.getGioBatDau().isAfter(thoiDiemHienTai)) {
                // lỡ giờ (hoặc đúng giờ nhưng đang trễ): xếp lại, không âm thầm bỏ ở quá khứ
                LocalTime somNhat = maxGio(thoiDiemHienTai, congTrongNgay(item.getGioBatDau(), soPhutTre));
                moc.put(item.getId(), somNhat);
                conLai.add(item);
            } else if (daBatDau) {
                daQua.add(item);
            } else {
                conLai.add(item);
            }
        }

        // SkipDisruption đã xử lý ở Bước 0; Delay KHÔNG còn dời hàng loạt (xem K-14).
        conLai.sort((a, b) -> a.getGioBatDau().compareTo(b.getGioBatDau()));

        // Bước 1.5: hook cho chiến lược muốn chủ động xử lý TRƯỚC khi quét
        // xung đột (vd: "Giảm tải" chủ động bỏ bớt việc phụ nếu lịch còn lại
        // rõ ràng quá dày, thay vì đợi "hết chỗ" mới xử lý như 2 chiến lược kia).
        conLai = tienXuLyTruocKhiQuet(conLai, thoiDiemHienTai, ketQua);

        // Bước 2: quét và giải quyết xung đột theo thứ tự ưu tiên.
        List<ScheduleItem> ketQuaCuoi = new ArrayList<>();
        Set<String> daGhi = new HashSet<>();
        LocalTime conTro = conTroBanDau;

        for (int i = 0; i < conLai.size(); i++) {
            ScheduleItem item = conLai.get(i);

            if (item instanceof FixedEvent) {
                if (item.getGioBatDau().isBefore(conTro)) {
                    ketQua.themCanhBao("Xung đột: các việc trước đó lấn vào giờ cố định \""
                        + item.getTen() + "\" (" + item.getGioBatDau() + " - " + item.getGioKetThuc() + ").");
                }
                conTro = maxGio(conTro, item.getGioKetThuc());
                ketQuaCuoi.add(item);
                continue;
            }

            Duration thoiLuong = item.layThoiLuong();
            LocalTime batDauMongMuon = maxGio(maxGio(item.getGioBatDau(), conTro),
                moc.getOrDefault(item.getId(), LocalTime.MIN));
            LocalTime gioiHanKe;
            if (moc.containsKey(item.getId())) {
                // Việc "lỡ giờ" bị đẩy muộn: (1) luôn nhảy qua việc cố định đang chắn đúng mốc mới;
                // (2) nếu không vừa trước việc cố định kế tiếp mà việc này KHÔNG hy sinh được thì xếp SAU
                // việc cố định đó (không chồng giờ); còn hy sinh được thì để luồng chung xử lý (đề xuất bỏ).
                batDauMongMuon = nhayQuaViecCoDinh(conLai, batDauMongMuon, Duration.ZERO);
                gioiHanKe = timGioBatDauFixedEventTuGio(conLai, batDauMongMuon);
                boolean khongVua = gioiHanKe != null
                    && batDauMongMuon.toSecondOfDay() + thoiLuong.getSeconds() > gioiHanKe.toSecondOfDay();
                if (khongVua && !coTheHySinh(item) && !(item instanceof RecurringEvent)) {
                    batDauMongMuon = nhayQuaViecCoDinh(conLai, batDauMongMuon, thoiLuong);
                    gioiHanKe = timGioBatDauFixedEventTuGio(conLai, batDauMongMuon);
                }
            } else {
                gioiHanKe = timGioBatDauFixedEventTiepTheo(conLai, i);
            }

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
                daGhi.add(item.getId());
                ketQua.themCanhBao("\"" + item.getTen() + "\" (" + item.getDoUuTien()
                    + ") vẫn bị dồn ép tới " + batDauMongMuon + " — có thể ảnh hưởng việc sau hoặc giờ ngủ.");
                conTro = ketThucMongMuon;
                ketQuaCuoi.add(item);
                continue;
            }

            if (!batDauMongMuon.equals(item.getGioBatDau())) {
                item.ganLaiKhungGioBoiEngine(batDauMongMuon, ketThucMongMuon);
                dongBoCanhBaoDeadlineNeuCan(item);
                ketQua.themThayDoi(moTaDoiGio(item, gioBanDau, batDauMongMuon, ketThucMongMuon));
                daGhi.add(item.getId());
            }
            conTro = ketThucMongMuon;
            ketQuaCuoi.add(item);
        }

        // K-13: lưới an toàn — mọi việc ĐỔI GIỜ so với lịch ban đầu (đối chiếu theo id) đều phải
        // có mặt trong danh sách thay đổi; việc không đổi giờ thì không tạo bản ghi giả.
        for (ScheduleItem it : ketQuaCuoi) {
            LocalTime[] cu = gioBanDau.get(it.getId());
            boolean doiGio = cu != null && (!cu[0].equals(it.getGioBatDau()) || !cu[1].equals(it.getGioKetThuc()));
            if (doiGio && !daGhi.contains(it.getId())) {
                ketQua.themThayDoi(moTaDoiGio(it, gioBanDau, it.getGioBatDau(), it.getGioKetThuc()));
            }
        }

        List<ScheduleItem> tongHop = new ArrayList<>(daQua);
        tongHop.addAll(ketQuaCuoi);
        lichSau.thayTheToanBo(tongHop);

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

    /** Cộng phút nhưng KHÔNG cuộn vòng qua 00:00 — chạm 23:59 là dừng (để bị coi là "vượt ngày"). */
    private static LocalTime congTrongNgay(LocalTime gio, int soPhut) {
        long giay = gio.toSecondOfDay() + soPhut * 60L;
        return giay >= CUOI_NGAY.toSecondOfDay() ? CUOI_NGAY : LocalTime.ofSecondOfDay(giay);
    }

    private static String moTaDoiGio(ScheduleItem item, Map<String, LocalTime[]> gioBanDau,
                                     LocalTime moiBatDau, LocalTime moiKetThuc) {
        LocalTime[] cu = gioBanDau.get(item.getId());
        String tu = cu == null ? "" : " từ " + cu[0] + " - " + cu[1];
        return "\"" + item.getTen() + "\": dời" + tu + " sang " + moiBatDau + " - " + moiKetThuc + ".";
    }

    private static LocalTime maxGio(LocalTime a, LocalTime b) {
        return a.isAfter(b) ? a : b;
    }

    /** Nếu khung [bd, bd+thoiLuong) đè lên việc cố định nào thì nhảy tới lúc việc đó kết thúc (lặp tới khi hết đè). */
    private static LocalTime nhayQuaViecCoDinh(List<ScheduleItem> danhSach, LocalTime bd, Duration thoiLuong) {
        boolean doi = true;
        while (doi) {
            doi = false;
            long ketThuc = bd.toSecondOfDay() + thoiLuong.getSeconds();
            for (ScheduleItem f : danhSach) {
                if (f instanceof FixedEvent && f.getGioKetThuc().isAfter(bd)
                    && f.getGioBatDau().toSecondOfDay() < ketThuc) {
                    bd = f.getGioKetThuc();
                    doi = true;
                }
            }
        }
        return bd;
    }

    private static LocalTime timGioBatDauFixedEventTuGio(List<ScheduleItem> danhSach, LocalTime tuGio) {
        LocalTime tot = null;
        for (ScheduleItem f : danhSach) {
            if (f instanceof FixedEvent && !f.getGioBatDau().isBefore(tuGio)
                && (tot == null || f.getGioBatDau().isBefore(tot))) {
                tot = f.getGioBatDau();
            }
        }
        return tot;
    }

    private static LocalTime timGioBatDauFixedEventTiepTheo(List<ScheduleItem> danhSach, int tuIndex) {
        for (int j = tuIndex + 1; j < danhSach.size(); j++) {
            if (danhSach.get(j) instanceof FixedEvent) {
                return danhSach.get(j).getGioBatDau();
            }
        }
        return null;
    }
}
