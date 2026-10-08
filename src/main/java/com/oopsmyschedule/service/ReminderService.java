package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.DeadlineTask;
import com.oopsmyschedule.domain.ScheduleItem;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reminder System (mục 4 tài liệu thiết kế mở rộng "Oops! My Schedule — Bản
 * mở rộng") — 2 loại nhắc CHỦ ĐỘNG (khác với {@link BoTheoDoiLich}, vốn chỉ
 * phản ứng SAU khi 1 việc đã trễ giờ):
 * <ul>
 *   <li><b>Sắp tới giờ</b>: còn &le; {@code phutTruocDeNhac} phút (mặc định
 *       15) là tới giờ bắt đầu 1 việc chưa hoàn thành.</li>
 *   <li><b>Deadline gần kề</b>: với {@link DeadlineTask}, còn 24h / 3h / 1h
 *       trước {@code hanChot}.</li>
 * </ul>
 * <p>
 * Lớp này CHỈ tính ra danh sách nội dung cần nhắc — thuần logic, không đụng
 * {@code java.awt.SystemTray} — để {@code KiemThu} kiểm được mà không cần
 * môi trường có màn hình (SystemTray không chạy được ở máy CI/sandbox
 * headless). Tầng UI ({@code MainFrame}) mới là nơi quyết định "bắn" thông
 * báo bằng cách nào: thử {@code SystemTray.displayMessage()} nếu máy hỗ trợ,
 * và LUÔN LUÔN ghi thêm vào {@link TrungTamThongBao} nội bộ của app để không
 * phụ thuộc hoàn toàn vào việc hệ điều hành có cho phép notification hay
 * không (nhiều bản Linux dùng để demo/chấm bài không có desktop notification
 * daemon — ghi log nội bộ là lưới an toàn).
 */
public class ReminderService {

    /** Các mốc nhắc deadline, tính theo số PHÚT còn lại trước hạn chót (24h, 3h, 1h). */
    private static final long[] MOC_DEADLINE_PHUT = {24 * 60L, 3 * 60L, 60L};

    private final int phutTruocDeNhac;

    /** Khóa "đã nhắc" (item::loại mốc) để không lặp lại mỗi lần quét — giống cơ chế của BoTheoDoiLich. */
    private final Set<String> daNhac = new HashSet<>();

    public ReminderService() {
        this(15);
    }

    public ReminderService(int phutTruocDeNhac) {
        if (phutTruocDeNhac <= 0) {
            throw new IllegalArgumentException("Số phút nhắc trước phải > 0.");
        }
        this.phutTruocDeNhac = phutTruocDeNhac;
    }

    /**
     * Quét lịch {@code ds} tại thời điểm {@code hienTai} của ngày
     * {@code ngayHomNay}, trả về danh sách nội dung nhắc MỚI phát sinh kể từ
     * lần quét gần nhất (rỗng nếu không có gì mới). Gọi định kỳ (vd mỗi giây
     * qua {@code javax.swing.Timer}, giống cách {@code MainFrame} đã dùng
     * cho {@link BoTheoDoiLich}).
     */
    public List<String> quet(DaySchedule ds, LocalDate ngayHomNay, LocalTime hienTai) {
        List<String> ketQua = new ArrayList<>();
        for (ScheduleItem item : ds.layLichHienTai()) {
            if (item.isHoanThanh()) {
                continue; // đã xong thì không cần nhắc gì thêm về việc này nữa
            }
            kiemTraSapToi(item, hienTai, ketQua);
            if (item instanceof DeadlineTask dt) {
                kiemTraDeadlineGanKe(dt, ngayHomNay, hienTai, ketQua);
            }
        }
        return ketQua;
    }

    private void kiemTraSapToi(ScheduleItem item, LocalTime hienTai, List<String> ketQua) {
        long phutConLai = Duration.between(hienTai, item.getGioBatDau()).toMinutes();
        // < 0 nghĩa là đã tới/qua giờ rồi -> đó là việc của BoTheoDoiLich (cảnh báo trễ), không phải nhắc trước.
        if (phutConLai < 0 || phutConLai > phutTruocDeNhac) {
            return;
        }
        String khoa = item.getId() + "::SAP_TOI";
        if (!daNhac.add(khoa)) {
            return; // đã nhắc việc này rồi
        }
        ketQua.add("⏰ Sắp tới: \"" + item.getTen() + "\" lúc " + item.getGioBatDau());
    }

    private void kiemTraDeadlineGanKe(DeadlineTask dt, LocalDate ngayHomNay, LocalTime hienTai, List<String> ketQua) {
        LocalDateTime bayGio = LocalDateTime.of(ngayHomNay, hienTai);
        long phutConLai = Duration.between(bayGio, dt.getHanChot()).toMinutes();
        if (phutConLai <= 0) {
            return; // đã trễ hạn -> DeadlineTask.canhBaoTreHan lo phần này, không phải Reminder
        }
        // Mỗi mốc chỉ "khớp" trong đúng 1 dải (băng) giữa nó và mốc kế tiếp NHỎ HƠN —
        // ví dụ mốc 3 tiếng chỉ khớp khi còn (1 tiếng; 3 tiếng], tránh việc còn đúng 3
        // tiếng mà lại báo nhầm "còn 24 tiếng" (mốc lớn hơn vẫn "còn hạn" theo kiểu <=).
        for (int i = 0; i < MOC_DEADLINE_PHUT.length; i++) {
            long moc = MOC_DEADLINE_PHUT[i];
            long capDuoi = (i + 1 < MOC_DEADLINE_PHUT.length) ? MOC_DEADLINE_PHUT[i + 1] : 0;
            if (phutConLai > moc || phutConLai <= capDuoi) {
                continue; // không nằm trong băng của mốc này
            }
            String khoa = dt.getId() + "::HAN_" + moc;
            if (!daNhac.add(khoa)) {
                continue;
            }
            ketQua.add("⚠️ Còn " + nhanMoc(moc) + " nữa hết hạn: \"" + dt.getTen() + "\"");
        }
    }

    private String nhanMoc(long phut) {
        return phut >= 60 ? (phut / 60) + " tiếng" : phut + " phút";
    }

    public int getPhutTruocDeNhac() {
        return phutTruocDeNhac;
    }

    /** Xoá toàn bộ trạng thái "đã nhắc" — dùng khi bắt đầu quét 1 ngày khác hẳn. */
    public void resetDaNhac() {
        daNhac.clear();
    }
}
