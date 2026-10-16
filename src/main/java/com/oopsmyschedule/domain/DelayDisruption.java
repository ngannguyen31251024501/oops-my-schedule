package com.oopsmyschedule.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lưu ý: engine tái sắp xếp KHÔNG gọi apDung để dời hàng loạt (xem AbstractReplanStrategy, K-14) —
 * chỉ dùng số phút trễ để xác định mốc sớm nhất cho việc đã lỡ giờ. apDung chỉ còn là API mức item
 * và không bao giờ cuộn vòng qua nửa đêm.
 *
 * Sự cố "Tôi bị trễ X phút" — dời từng item còn dời được, giữ nguyên item cố định. */
public class DelayDisruption extends Disruption {

    private final int soPhutTre;
    /** Việc dời sẽ vượt 23:59 nên KHÔNG bị dời; Strategy đọc để đề xuất sang ngày mai. */
    private final List<ScheduleItem> viecVuotNgay = new ArrayList<>();

    public DelayDisruption(int soPhutTre) {
        super("Bị trễ " + soPhutTre + " phút");
        if (soPhutTre <= 0) {
            throw new IllegalArgumentException("Số phút trễ phải > 0.");
        }
        if (soPhutTre > 1440) { // INV-16
            throw new IllegalArgumentException("Số phút trễ không được quá 1440 (1 ngày).");
        }
        this.soPhutTre = soPhutTre;
    }

    public int getSoPhutTre() {
        return soPhutTre;
    }

    @Override
    public void apDung(List<ScheduleItem> danhSachConLai) {
        viecVuotNgay.clear(); // engine dùng chung 1 disruption cho 3 strategy
        for (ScheduleItem item : danhSachConLai) {
            // Chỉ gọi xuLyKhiBiTre() cho item nào tự nhận là dời được — tránh
            // gọi thẳng vào FixedEvent (sẽ ném IllegalStateException, đúng ý
            // thiết kế: exception là để bắt lỗi lập trình, không phải luồng bình thường).
            if (item.isHoanThanh() || !item.coTheDoiLich()) { // INV-18: việc đã xong không bị dời
                continue;
            }
            if (!item.doiDuocTrongNgay(soPhutTre)) { // K-14: không cuộn vòng qua nửa đêm
                viecVuotNgay.add(item); // không dời, giữ nguyên giờ
                continue;
            }
            item.xuLyKhiBiTre(soPhutTre);
        }
    }

    /** Các việc bị bỏ qua ở lần {@link #apDung} gần nhất vì dời sẽ vượt 23:59. */
    public List<ScheduleItem> getViecVuotNgay() {
        return Collections.unmodifiableList(new ArrayList<>(viecVuotNgay));
    }
}