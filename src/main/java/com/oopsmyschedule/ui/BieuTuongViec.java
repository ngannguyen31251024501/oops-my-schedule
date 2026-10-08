package com.oopsmyschedule.ui;

import com.oopsmyschedule.domain.DeadlineTask;
import com.oopsmyschedule.domain.RecurringEvent;
import com.oopsmyschedule.domain.ScheduleItem;

import java.util.Locale;

/**
 * Chọn 1 icon (emoji) đại diện cho từng {@link ScheduleItem}, để mọi màn
 * hình (Timeline, Trang chủ, So sánh lịch trình, Báo sự cố...) đều hiển thị
 * cùng 1 icon nhất quán cho cùng 1 việc — giúp giao diện trực quan, dễ quét
 * mắt hơn thay vì chỉ có chữ.
 * <p>
 * Deadline / Lặp lại có icon cố định theo LOẠI (đã tự nói lên bản chất);
 * các việc còn lại được "đoán" icon theo từ khóa trong tên việc, giống cách
 * các app lịch phổ biến vẫn làm.
 */
public final class BieuTuongViec {
    private BieuTuongViec() {}

    public static String cua(ScheduleItem item) {
        if (item instanceof DeadlineTask) return "⏰";
        if (item instanceof RecurringEvent) return "🔁";
        return theoTenViec(item.getTen(), item.loaiViec());
    }

    private static String theoTenViec(String tenGoc, String loaiViec) {
        String ten = tenGoc.toLowerCase(Locale.forLanguageTag("vi"));

        if (chua(ten, "thể dục", "gym", "chạy bộ", "yoga", "bơi")) return "🏃";
        if (chua(ten, "tắm", "vệ sinh")) return "🚿";
        if (chua(ten, "ngủ", "nghỉ trưa", "nghỉ ngơi")) return "😴";
        if (chua(ten, "bài tập", "làm bài")) return "📝";
        if (chua(ten, "học", "ôn thi", "ôn bài", "lên lớp")) return "📖";
        if (chua(ten, "project", "đồ án", "dự án", "lập trình", "code")) return "💻";
        if (chua(ten, "phim", "movie")) return "🎬";
        if (chua(ten, "họp", "meeting")) return "👥";
        if (chua(ten, "đọc", "sách", "truyện")) return "📚";
        if (chua(ten, "nấu", "ăn sáng", "ăn trưa", "ăn tối", "ăn cơm", "cơm")) return "🍽️";
        if (chua(ten, "chơi", "game")) return "🎮";
        if (chua(ten, "nhạc", "đàn", "guitar", "piano")) return "🎵";
        if (chua(ten, "mua", "chợ", "siêu thị")) return "🛒";
        if (chua(ten, "xe", "di chuyển", "đi lại")) return "🚗";

        return "Cố định".equals(loaiViec) ? "📌" : "🗒️";
    }

    private static boolean chua(String ten, String... tuKhoa) {
        for (String tk : tuKhoa) {
            if (ten.contains(tk)) return true;
        }
        return false;
    }
}
