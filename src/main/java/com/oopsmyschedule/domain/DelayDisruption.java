package com.oopsmyschedule.domain;

import java.util.List;

/**
 * Lưu ý: engine tái sắp xếp KHÔNG gọi apDung để dời hàng loạt (xem AbstractReplanStrategy, K-14) —
 * chỉ dùng số phút trễ để xác định mốc sớm nhất cho việc đã lỡ giờ. apDung chỉ còn là API mức item
 * và không bao giờ cuộn vòng qua nửa đêm.
 *
 * Sự cố "Tôi bị trễ X phút" — dời từng item còn dời được, giữ nguyên item cố định. */
public class DelayDisruption extends Disruption {

    private final int soPhutTre;

    public DelayDisruption(int soPhutTre) {
        super("Bị trễ " + soPhutTre + " phút");
        if (soPhutTre <= 0) {
            throw new IllegalArgumentException("Số phút trễ phải > 0.");
        }
        this.soPhutTre = soPhutTre;
    }

    public int getSoPhutTre() {
        return soPhutTre;
    }

    @Override
    public void apDung(List<ScheduleItem> danhSachConLai) {
        for (ScheduleItem item : danhSachConLai) {
            // Chỉ gọi xuLyKhiBiTre() cho item nào tự nhận là dời được — tránh
            // gọi thẳng vào FixedEvent (sẽ ném IllegalStateException, đúng ý
            // thiết kế: exception là để bắt lỗi lập trình, không phải luồng bình thường).
            if (item.coTheDoiLich() && item.doiDuocTrongNgay(soPhutTre)) {
                item.xuLyKhiBiTre(soPhutTre);
            }
        }
    }
}
