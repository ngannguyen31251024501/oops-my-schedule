package com.oopsmyschedule;

import com.oopsmyschedule.domain.*;
import com.oopsmyschedule.service.DaySchedule;
import com.oopsmyschedule.service.KhoDuLieu;
import com.oopsmyschedule.ui.MainFrame;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        LocalDate homNay = LocalDate.now();

        // Đã có dữ liệu lưu từ lần chạy trước (tự động, không cần người dùng
        // chọn file) thì dùng luôn, khỏi cần tạo lại lịch demo mẫu.
        //
        // FIX BUG (audit bản 10, mục 3): trước đây file lưu bị hỏng (JSON lỗi)
        // sẽ ÂM THẦM bị coi như "chưa có dữ liệu" rồi tạo đè lịch mẫu mới lên
        // trên — nguy cơ mất dữ liệu người dùng. Giờ báo lỗi rõ ràng, và KHÔNG
        // tự động lưu đè: chỉ mở app với 1 lịch mẫu TẠM trong RAM, người dùng
        // tự quyết định làm gì tiếp theo (file gốc trên đĩa vẫn còn nguyên).
        DaySchedule daLuu;
        try {
            daLuu = KhoDuLieu.docNeuCo(homNay);
        } catch (KhoDuLieu.DuLieuHongException ex) {
            JOptionPane.showMessageDialog(null,
                ex.getMessage() + "\nApp sẽ mở với 1 lịch tạm — dữ liệu cũ trên đĩa KHÔNG bị ghi đè cho tới khi bạn tự lưu.",
                "Không thể đọc dữ liệu đã lưu", JOptionPane.WARNING_MESSAGE);
            daLuu = null;
        }
        if (daLuu != null) {
            DaySchedule daLuuCuoi = daLuu; // effectively-final cho lambda bên dưới
            SwingUtilities.invokeLater(() -> new MainFrame(daLuuCuoi).setVisible(true));
            return;
        }

        DaySchedule ds = new DaySchedule(homNay);

        // ---- Buổi sáng ----
        ds.themItem(new FlexibleTask("Tắm rửa", LocalTime.of(6, 0), LocalTime.of(6, 20), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Ăn sáng", LocalTime.of(6, 20), LocalTime.of(6, 50), PriorityLevel.NEN_LAM));

        // Buổi học chính (cố định, không thể dời) — 7h10, 4 tiếng
        ds.themItem(new FixedEvent("Học môn A ở trường", LocalTime.of(7, 10), LocalTime.of(11, 10), PriorityLevel.BAT_BUOC));

        // ---- Buổi trưa ----
        ds.themItem(new FlexibleTask("Nấu cơm", LocalTime.of(11, 10), LocalTime.of(11, 40), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Ăn trưa", LocalTime.of(11, 40), LocalTime.of(12, 10), PriorityLevel.NEN_LAM));

        // ---- Buổi chiều: chạy deadline các môn ----
        ds.themItem(new DeadlineTask("Deadline An ninh thông tin", LocalTime.of(13, 0), LocalTime.of(14, 30),
            PriorityLevel.BAT_BUOC, LocalDateTime.of(homNay, LocalTime.of(23, 59))));

        ds.themItem(new FlexibleTask("Đọc truyện giải trí", LocalTime.of(14, 30), LocalTime.of(15, 30), PriorityLevel.CO_THE_BO));

        ds.themItem(new DeadlineTask("Deadline Lập trình ứng dụng", LocalTime.of(15, 30), LocalTime.of(17, 0),
            PriorityLevel.NEN_LAM, LocalDateTime.of(homNay.plusDays(1), LocalTime.of(23, 59))));

        // ---- Chiều tối ----
        ds.themItem(new RecurringEvent("Học tiếng Anh", LocalTime.of(17, 0), LocalTime.of(17, 30), PriorityLevel.NEN_LAM, 5));
        ds.themItem(new FlexibleTask("Tắm rửa buổi tối", LocalTime.of(17, 30), LocalTime.of(18, 0), PriorityLevel.NEN_LAM));
        ds.themItem(new FlexibleTask("Ăn tối", LocalTime.of(18, 0), LocalTime.of(18, 30), PriorityLevel.NEN_LAM));

        ds.themItem(new DeadlineTask("Deadline Phân tích dữ liệu", LocalTime.of(18, 30), LocalTime.of(20, 0),
            PriorityLevel.CO_THE_BO, LocalDateTime.of(homNay.plusDays(2), LocalTime.of(23, 59))));

        // ---- Tối muộn: chuẩn bị đi ngủ (cố định, đảm bảo giờ ngủ không bị dồn ép) ----
        ds.themItem(new FixedEvent("Chuẩn bị đi ngủ", LocalTime.of(23, 0), LocalTime.of(23, 30), PriorityLevel.BAT_BUOC));

        // Gợi ý: các việc như "Đi xe từ quê lên Sài Gòn" (Cố định) hoặc buổi học
        // 12h45 (thay cho 7h10) có thể tự thêm bằng nút "+ Thêm việc" trong app,
        // vì đây là những việc không xảy ra mỗi ngày nên không seed sẵn ở đây.

        SwingUtilities.invokeLater(() -> new MainFrame(ds).setVisible(true));
    }
}
