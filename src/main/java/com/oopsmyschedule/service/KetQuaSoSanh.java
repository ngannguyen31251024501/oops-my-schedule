package com.oopsmyschedule.service;

import com.oopsmyschedule.domain.ScheduleItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Kết quả trả về từ {@link ReplanEngine#reSapXep}: lịch trước, lịch sau,
 * danh sách thay đổi, danh sách đề xuất bỏ, và danh sách cảnh báo (ví dụ
 * việc "Bắt buộc" vẫn bị lố giờ sau khi đã tái sắp xếp hết mức có thể).
 */
public class KetQuaSoSanh {

    private final DaySchedule lichTruoc;
    private final DaySchedule lichSau;
    private final List<String> danhSachThayDoi = new ArrayList<>();
    private final List<ScheduleItem> danhSachDeXuatBo = new ArrayList<>();
    private final List<String> danhSachCanhBao = new ArrayList<>();

    public KetQuaSoSanh(DaySchedule lichTruoc, DaySchedule lichSau) {
        this.lichTruoc = lichTruoc;
        this.lichSau = lichSau;
    }

    public void themThayDoi(String moTa) {
        danhSachThayDoi.add(moTa);
    }

    public void themDeXuatBo(ScheduleItem item) {
        danhSachDeXuatBo.add(item);
    }

    public void themCanhBao(String moTa) {
        danhSachCanhBao.add(moTa);
    }

    public DaySchedule getLichTruoc() {
        return lichTruoc;
    }

    public DaySchedule getLichSau() {
        return lichSau;
    }

    public List<String> getDanhSachThayDoi() {
        return danhSachThayDoi;
    }

    public List<ScheduleItem> getDanhSachDeXuatBo() {
        return danhSachDeXuatBo;
    }

    public List<String> getDanhSachCanhBao() {
        return danhSachCanhBao;
    }

    public boolean coCanhBao() {
        return !danhSachCanhBao.isEmpty();
    }
}
