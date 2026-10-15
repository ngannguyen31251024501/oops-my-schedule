package com.oopsmyschedule.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Sự cố "Tôi bị trễ X phút" — dờii từng item còn dời được, giữ nguyên item cố định. */
public class DelayDisruption extends Disruption {

    private final int soPhutTre;
    /** Việc dời sẽ vượt 23:59 nên KHÔNG bị dời; Strategy đọc để đề xuất sang ngày mai. */
    private final List<ScheduleItem> viecVuotNgay = new ArrayList<>();

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
        viecVuotNgay.clear(); // engine dùng chung 1 disruption cho 3 strategy
        for (ScheduleItem item : danhSachConLai) {
            // Chỉ gọi xuLyKhiBiTre() cho item nào tự nhận là dời được — tránh
            // gọi thẳng vào FixedEvent (sẽ ném IllegalStateException, đúng ý
            // thiết kế: exception là để bắt lỗi lập trình, không phải luồng bình thường).
            if (!item.coTheDoiLich()) {
                continue;
            }
            if (item.vuotNuaDemNeuDoi(soPhutTre)) {
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