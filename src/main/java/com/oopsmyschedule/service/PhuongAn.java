package com.oopsmyschedule.service;

/**
 * 1 phương án cụ thể do 1 {@link ReplanStrategy} tính ra — gói chung nhãn
 * hiển thị (tên, icon, mô tả ngắn) với {@link KetQuaSoSanh} tương ứng, để UI
 * (CanhBaoTreDialog) có đủ dữ liệu vẽ 3 thẻ phương án cho người dùng so
 * sánh & chọn.
 */
public class PhuongAn {

    private final String ten;
    private final String bieuTuong;
    private final String moTa;
    private final KetQuaSoSanh ketQua;

    public PhuongAn(String ten, String bieuTuong, String moTa, KetQuaSoSanh ketQua) {
        this.ten = ten;
        this.bieuTuong = bieuTuong;
        this.moTa = moTa;
        this.ketQua = ketQua;
    }

    public String getTen() {
        return ten;
    }

    public String getBieuTuong() {
        return bieuTuong;
    }

    public String getMoTa() {
        return moTa;
    }

    public KetQuaSoSanh getKetQua() {
        return ketQua;
    }
}
