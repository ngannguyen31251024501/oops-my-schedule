package com.oopsmyschedule.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sự cố "Tôi bị trễ X phút" — dời từng item còn dời được, giữ nguyên item cố định.
 * Bỏ qua: việc đã hoàn thành, RecurringEvent đã bỏ qua hôm nay, và việc sẽ vượt 23:59
 * (những việc vượt được ghi vào {@link #getViecVuotNgay()} để Strategy đề xuất sang ngày mai).
 */
public class DelayDisruption extends Disruption {

    /** INV-16: tối đa 1 ngày. */
    private static final int SO_PHUT_TOI_DA = 1440;

    private final int soPhutTre;
    private final List<ScheduleItem> viecVuotNgay = new ArrayList<>();

    public DelayDisruption(int soPhutTre) {
        super("Bị trễ " + soPhutTre + " phút");
        if (soPhutTre <= 0) {
            throw new IllegalArgumentException("Số phút trễ phải > 0.");
        }
        if (soPhutTre > SO_PHUT_TOI_DA) {
            throw new IllegalArgumentException("Số phút trễ không được quá " + SO_PHUT_TOI_DA + ".");
        }
        this.soPhutTre = soPhutTre;
    }

    public int getSoPhutTre() {
        return soPhutTre;
    }

    /** Các việc bị bỏ qua ở lần apDung gần nhất vì dời sẽ vượt 23:59 (chưa bị dời). */
    public List<ScheduleItem> getViecVuotNgay() {
        return Collections.unmodifiableList(viecVuotNgay);
    }

    @Override
    public void apDung(List<ScheduleItem> danhSachConLai) {
        viecVuotNgay.clear();   // cùng 1 sự cố được 3 chiến lược dùng lại, tránh cộng dồn
        for (ScheduleItem item : danhSachConLai) {
            if (!item.coTheDoiLich() || item.isHoanThanh()) {
                continue;
            }
            if (item instanceof RecurringEvent re && re.isBoQuaHomNay()) {
                continue;
            }
            if (item.vuotNuaDemNeuDoi(soPhutTre)) {
                viecVuotNgay.add(item);
                continue;
            }
            item.xuLyKhiBiTre(soPhutTre);
        }
    }
}