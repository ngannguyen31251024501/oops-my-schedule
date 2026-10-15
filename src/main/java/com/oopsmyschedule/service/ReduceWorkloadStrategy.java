package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.FixedEvent;
import com.oopsmyschedule.domain.PriorityLevel;
import com.oopsmyschedule.domain.ScheduleItem;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Phương án 3 — "Giảm tải": nếu tổng thời lượng các việc còn lại rõ ràng
 * không thể vừa hết quỹ thời gian còn lại trong ngày, CHỦ ĐỘNG đề xuất bỏ
 * bớt việc "Có thể bỏ" NGAY TỪ ĐẦU (trước khi quét xung đột), thay vì cố
 * nhồi nhét hết mọi việc sát nhau rồi mới xử lý khi "hết chỗ" như 2 chiến
 * lược kia. Trong lúc quét, cũng dễ đề xuất bỏ hơn (cả "Có thể bỏ" lẫn "Nên
 * làm", trừ việc "Bắt buộc" và việc cố định) — ưu tiên một lịch bớt dồn dập
 * hơn là giữ đủ số lượng việc.
 */
public class ReduceWorkloadStrategy extends AbstractReplanStrategy {

    @Override
    public String getTen() {
        return "Giảm tải";
    }

    @Override
    public String getBieuTuong() {
        return "🌿";
    }

    @Override
    public String getMoTaNgan() {
        return "Chủ động bỏ bớt việc ít quan trọng để lịch còn lại trong ngày bớt dồn dập, đỡ áp lực.";
    }

    @Override
    protected boolean coTheHySinh(ScheduleItem item) {
        if (item instanceof FixedEvent) {
            return false;
        }
        return item.getDoUuTien() != PriorityLevel.BAT_BUOC;
    }

    @Override
    protected List<ScheduleItem> tienXuLyTruocKhiQuet(List<ScheduleItem> conLai, LocalTime thoiDiemHienTai,
                                                        KetQuaSoSanh ketQua) {
        long phutCoSan = tinhPhutCoSanThucTe(conLai, thoiDiemHienTai);
        long phutCanThiet = conLai.stream()
                .filter(it -> !(it instanceof FixedEvent) && !it.isHoanThanh())
                .mapToLong(it -> it.layThoiLuong().toMinutes())
                .sum();
        if (phutCanThiet <= phutCoSan) {
            return conLai; // lịch chưa quá tải, không cần chủ động cắt bớt
        }

        List<ScheduleItem> ketQuaConLai = new ArrayList<>(conLai);
        // Vòng 1: ưu tiên bỏ những việc "Có thể bỏ" trước.
        phutCanThiet = giamTai(ketQuaConLai, phutCanThiet, phutCoSan, PriorityLevel.CO_THE_BO, ketQua);
        // Vòng 2: nếu vẫn còn quá tải, mới tính tới việc "Nên làm" (không phải cố định/bắt buộc).
        if (phutCanThiet > phutCoSan) {
            giamTai(ketQuaConLai, phutCanThiet, phutCoSan, PriorityLevel.NEN_LAM, ketQua);
        }
        return ketQuaConLai;
    }

    /**
     * FIX (audit bản 12, mục 4): trước đây coi TOÀN BỘ khoảng "23:59 -
     * thoiDiemHienTai" là quỹ thời gian khả dụng, kể cả khi phần lớn khoảng
     * đó đã bị FixedEvent chiếm chỗ (VD: 12:00–18:00 đã có việc cố định) ->
     * ước lượng quá lạc quan, dẫn tới quyết định "chưa quá tải" sai. Nay chỉ
     * cộng dồn các khoảng TRỐNG thực tế giữa các FixedEvent (từ hiện tại đến
     * cuối ngày), giống cách {@code DaySchedule#timKhoangTrongDauTien} dò khe
     * hở — khác là ở đây cộng dồn TẤT CẢ khe hở, không chỉ khe đầu tiên, vì
     * mục đích là ước lượng tổng quỹ thời gian còn dùng được, không phải tìm
     * 1 chỗ để nhét 1 việc.
     */
    private long tinhPhutCoSanThucTe(List<ScheduleItem> conLai, LocalTime thoiDiemHienTai) {
        List<ScheduleItem> daKhoaTheoGio = conLai.stream()
            .filter(it -> it instanceof FixedEvent)
            .sorted((a, b) -> a.getGioBatDau().compareTo(b.getGioBatDau()))
            .toList();

        long phutTrong = 0;
        LocalTime conTro = thoiDiemHienTai;
        for (ScheduleItem fe : daKhoaTheoGio) {
            LocalTime batDauKhoa = fe.getGioBatDau();
            LocalTime ketThucKhoa = fe.getGioKetThuc();
            if (!ketThucKhoa.isAfter(conTro)) {
                continue; // việc cố định này đã qua rồi, không còn chắn thời gian phía trước
            }
            if (batDauKhoa.isAfter(conTro)) {
                phutTrong += Duration.between(conTro, batDauKhoa).toMinutes();
            }
            if (ketThucKhoa.isAfter(conTro)) {
                conTro = ketThucKhoa;
            }
        }
        if (CUOI_NGAY.isAfter(conTro)) {
            phutTrong += Duration.between(conTro, CUOI_NGAY).toMinutes();
        }
        return phutTrong;
    }

    private long giamTai(List<ScheduleItem> danhSach, long phutCanThiet, long phutCoSan,
                          PriorityLevel mucNham, KetQuaSoSanh ketQua) {
        Iterator<ScheduleItem> it = danhSach.iterator();
        while (phutCanThiet > phutCoSan && it.hasNext()) {
            ScheduleItem item = it.next();
            if (item instanceof FixedEvent || item.getDoUuTien() != mucNham) {
                continue;
            }
            it.remove();
            phutCanThiet -= item.layThoiLuong().toMinutes();
            ketQua.themDeXuatBo(item);
            ketQua.themThayDoi("\"" + item.getTen() + "\": chủ động đề xuất BỎ để giảm tải cho lịch hôm nay.");
        }
        return phutCanThiet;
    }
}
