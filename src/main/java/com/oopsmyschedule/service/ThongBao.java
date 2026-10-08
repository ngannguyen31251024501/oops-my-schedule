package com.oopsmyschedule.service;

import java.time.LocalTime;

/** 1 thông báo mà app đã gửi cho người dùng — nội dung + giờ gửi + đã đọc hay chưa. */
public class ThongBao {

    private final String noiDung;
    private final LocalTime gio;
    private boolean daDoc = false;

    public ThongBao(String noiDung, LocalTime gio) {
        this.noiDung = noiDung;
        this.gio = gio;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public LocalTime getGio() {
        return gio;
    }

    public boolean isDaDoc() {
        return daDoc;
    }

    public void danhDauDaDoc() {
        this.daDoc = true;
    }
}
