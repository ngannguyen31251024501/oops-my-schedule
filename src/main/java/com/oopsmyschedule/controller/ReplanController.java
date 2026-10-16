package com.oopsmyschedule.controller;

import com.oopsmyschedule.domain.Disruption;
import com.oopsmyschedule.domain.ScheduleItem;
import com.oopsmyschedule.service.DaySchedule;
import com.oopsmyschedule.service.KetQuaApDung;
import com.oopsmyschedule.service.KetQuaSoSanh;
import com.oopsmyschedule.service.KhoDuLieu;
import com.oopsmyschedule.service.PhuongAn;
import com.oopsmyschedule.service.ReplanApplyService;
import com.oopsmyschedule.service.ReplanEngine;
import com.oopsmyschedule.service.SuCoNhatKy;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;

/**
 * Điều phối việc tạo và áp dụng phương án sắp xếp lại lịch.
 * Controller chỉ chuyển tiếp: luật nghiệp vụ nằm ở ReplanEngine / ReplanApplyService.
 */
public class ReplanController {

    private final ReplanEngine engine;
    private final ReplanApplyService applyService = new ReplanApplyService();
    private final Function<LocalDate, DaySchedule> layLich;

    public ReplanController(ReplanEngine engine, Function<LocalDate, DaySchedule> layLich) {
        if (engine == null || layLich == null) {
            throw new IllegalArgumentException("engine và layLich không được null.");
        }
        this.engine = engine;
        this.layLich = layLich;
    }

    /**
     * Tạo các phương án để giao diện hiển thị. Không đổi lịch thật.
     */
    public List<PhuongAn> taoPhuongAn(DaySchedule lichHomNay, Disruption suCo, LocalTime thoiDiemHienTai) {
        if (lichHomNay == null || suCo == null || thoiDiemHienTai == null) {
            throw new IllegalArgumentException("Thiếu dữ liệu để tạo phương án.");
        }
        return engine.taoCacPhuongAn(lichHomNay, suCo, thoiDiemHienTai);
    }

    /**
     * Áp dụng phương án người dùng đã chọn, ghi nhật ký rồi lưu cả hôm nay và ngày mai.
     */
    public KetQuaApDung apDungPhuongAn(PhuongAn phuongAn, DaySchedule lichHomNay,
                                       LocalTime thoiDiemHienTai, String tieuDeSuCo) throws IOException {
        if (phuongAn == null || lichHomNay == null || thoiDiemHienTai == null) {
            throw new IllegalArgumentException("Thiếu dữ liệu để áp dụng phương án.");
        }
        KetQuaSoSanh ketQua = phuongAn.getKetQua();
        if (ketQua == null || ketQua.getLichSau() == null) {
            throw new IllegalArgumentException("Phương án không có lịch kết quả hợp lệ.");
        }
        DaySchedule lichNgayMai = layLich.apply(lichHomNay.getNgay().plusDays(1));
        if (lichNgayMai == null) {
            throw new IllegalStateException("Không tìm thấy lịch ngày mai.");
        }

        KetQuaApDung kq = applyService.apDung(lichHomNay, lichNgayMai, phuongAn);
        // Việc không chuyển được sang ngày mai thì giữ lại ở lịch hôm nay, không để mất.
        for (ScheduleItem bi : kq.getChuaXepLich()) {
            giuLaiHomNay(lichHomNay, bi);
        }

        List<String> tenChuaXep = kq.getChuaXepLich().stream().map(ScheduleItem::getTen).toList();
        lichHomNay.ghiNhatKySuCo(new SuCoNhatKy(thoiDiemHienTai, tieuDeSuCo,
                phuongAn.getTen(), ketQua.getDanhSachThayDoi(), tenChuaXep));

        KhoDuLieu.luuNgay(lichHomNay);
        KhoDuLieu.luuNgay(lichNgayMai);
        return kq;
    }

    /**
     * Việc không chuyển được sang ngày mai thì giữ lại ở lịch hôm nay, không để mất.
     */
    static void giuLaiHomNay(DaySchedule lichHomNay, ScheduleItem bi) {
        try {
            if (!lichHomNay.layLichHienTai().contains(bi)) {
                lichHomNay.themItem(bi);
            }
        } catch (IllegalArgumentException ignored) {
            // trùng việc cố định: vẫn được ghi trong nhật ký sự cố + thông báo "chưa thể sắp xếp"
        }
    }
}