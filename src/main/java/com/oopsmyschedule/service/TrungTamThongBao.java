package com.oopsmyschedule.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * "Trung tâm thông báo" — nơi lưu lại mọi lời nhắc app đã từng gửi cho người
 * dùng (trễ lịch, đã áp dụng lịch mới, v.v.), để người dùng có thể bấm 🔔 xem
 * lại bất cứ lúc nào thay vì popup thoáng qua rồi mất luôn.
 * <p>
 * Giữ tối đa {@link #GIOI_HAN} thông báo gần nhất (mới nhất lên đầu) để danh
 * sách không phình vô hạn trong 1 phiên demo dài.
 */
public class TrungTamThongBao {

    private static final int GIOI_HAN = 50;

    private final List<ThongBao> danhSach = new ArrayList<>();

    /** Thêm 1 thông báo mới, luôn chèn lên đầu danh sách (mới nhất trước). */
    public void them(String noiDung, LocalTime gio) {
        danhSach.add(0, new ThongBao(noiDung, gio));
        while (danhSach.size() > GIOI_HAN) {
            danhSach.remove(danhSach.size() - 1);
        }
    }

    /** Danh sách chỉ đọc, mới nhất trước. */
    public List<ThongBao> layDanhSach() {
        return Collections.unmodifiableList(danhSach);
    }

    /** Số thông báo CHƯA đọc — dùng làm huy hiệu số trên nút 🔔. */
    public int soLuong() {
        return (int) danhSach.stream().filter(t -> !t.isDaDoc()).count();
    }

    /** Đánh dấu toàn bộ đã đọc — gọi khi người dùng mở dialog xem thông báo. */
    public void danhDauDaDocHet() {
        danhSach.forEach(ThongBao::danhDauDaDoc);
    }
}
