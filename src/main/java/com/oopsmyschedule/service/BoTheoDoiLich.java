package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.ScheduleItem;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Mô phỏng ý tưởng "app tự nhìn đồng hồ hệ thống, không cần bấm nút 'Tôi bị
 * trễ'" đã bàn: quét toàn bộ lịch mỗi lần gọi {@link #quet}, so giờ hiện tại
 * (lấy từ {@link DongHo} — thật hoặc mô phỏng) với giờ bắt đầu từng việc.
 * <p>
 * Nguyên tắc quan trọng: lớp này CHỈ báo hiệu ("việc X đã tới giờ mà chưa
 * xong"), KHÔNG tự động gọi ReplanEngine. Quyết định "tôi đang trễ, cứu lịch
 * giúp tôi" luôn do người dùng bấm xác nhận ở UI — tránh app tự phán đoán
 * quá mức khi người dùng có thể chỉ đang nghỉ ngắn / cố tình chưa làm.
 */
public final class BoTheoDoiLich {

    private BoTheoDoiLich() {
    }

    /** Quét danh sách việc, trả về cảnh báo cho mỗi việc đã tới giờ mà chưa hoàn thành. */
    public static List<CanhBaoViec> quet(DaySchedule ds, LocalTime hienTai) {
        return quet(ds, hienTai, Set.of());
    }

    /**
     * Như {@link #quet(DaySchedule, LocalTime)} nhưng bỏ qua các việc có id nằm trong
     * {@code idBoQua} (người dùng bấm "Bỏ qua" ở banner: chỉ ngừng nhắc, KHÔNG đánh dấu
     * hoàn thành, KHÔNG xóa việc).
     */
    public static List<CanhBaoViec> quet(DaySchedule ds, LocalTime hienTai, Set<String> idBoQua) {
        if (ds == null || hienTai == null || idBoQua == null) {
            throw new IllegalArgumentException("ds, hienTai, idBoQua không được null.");
        }
        List<CanhBaoViec> ketQua = new ArrayList<>();
        for (ScheduleItem item : ds.layLichHienTai()) {
            if (item.isHoanThanh() || idBoQua.contains(item.getId())) {
                continue;
            }
            if (item.getGioBatDau().isAfter(hienTai)) {
                continue; // chưa tới giờ, chưa cần nhắc
            }
            long phutTre = Duration.between(item.getGioBatDau(), hienTai).toMinutes();
            ketQua.add(new CanhBaoViec(item, phutTre, MucDoCanhBao.phanLoai(phutTre)));
        }
        return ketQua;
    }
}
