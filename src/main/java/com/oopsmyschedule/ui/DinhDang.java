package com.oopsmyschedule.ui;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
/**
 * Định dạng thời lượng tính bằng phút thành chuỗi dễ đọc — vì hiển thị thẳng
 * "trễ 878 phút" nhìn rất "ghê" và khó hình dung, trong khi "14 giờ 38 phút"
 * thì người dùng đọc phát hiểu ngay.
 * <p>
 * Quy tắc: dưới 60 phút thì giữ nguyên "X phút" (không cần đổi); từ 60 phút
 * trở lên thì đổi sang "X giờ Y phút" (bỏ "Y phút" nếu Y = 0, vd "2 giờ").
 */
public final class DinhDang {

    private DinhDang() {
    }

    public static String phut(long soPhut) {
        if (soPhut < 0) {
            soPhut = 0;
        }
        if (soPhut < 60) {
            return soPhut + " phút";
        }
        long gio = soPhut / 60;
        long phutConLai = soPhut % 60;
        if (phutConLai == 0) {
            return gio + " giờ";
        }
        return gio + " giờ " + phutConLai + " phút";
    }

    /**
     * FIX (feedback Visual Design: "Nếu nội dung nằm trong khung thì ổn
     * hơn."): tên việc dài (VD "Làm bài tập môn Lập trình hướng đối tượng")
     * có thể tràn khỏi panel/label vì JLabel mặc định không tự xuống dòng
     * hay rút gọn. Hàm này rút gọn còn tối đa {@code doDaiToiDa} ký tự kèm
     * "…" — dùng chung cho mọi nơi hiển thị tên việc trên các khung hẹp
     * (timeline, toast, dialog...). Nên gọi kèm {@code JComponent#setToolTipText}
     * với chuỗi gốc để người dùng vẫn xem được đầy đủ khi rê chuột vào.
     */
    public static String rutGon(String text, int doDaiToiDa) {
        if (text == null || text.length() <= doDaiToiDa) {
            return text;
        }
        return text.substring(0, Math.max(0, doDaiToiDa - 1)).trim() + "…";
    }

    /** Định dạng giờ hiển thị gọn "HH:mm" (bỏ giây và phần lẻ). */
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");

    public static String gio(LocalTime thoiGian) {
        return thoiGian == null ? "" : thoiGian.format(GIO);
    }
}