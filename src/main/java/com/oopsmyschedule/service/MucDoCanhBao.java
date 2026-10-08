package com.oopsmyschedule.service;

/**
 * 3 tầng cảnh báo khi 1 việc đã tới/qua giờ mà chưa được đánh dấu hoàn thành.
 * Ngưỡng phút lấy theo đúng ví dụ đã bàn: 0-5' chỉ nhắc nhẹ, 5-20' cảnh báo,
 * &gt;20' đề xuất Replan ngay.
 */
public enum MucDoCanhBao {
    MOI_BAT_DAU("🕐 Đã bắt đầu", 0),
    TRE_NHE("⚠️ Đang trễ", 5),
    TRE_NANG("🚨 Nguy cơ vỡ lịch", 20);

    private final String nhan;
    private final int nguongPhut;

    MucDoCanhBao(String nhan, int nguongPhut) {
        this.nhan = nhan;
        this.nguongPhut = nguongPhut;
    }

    public String getNhan() {
        return nhan;
    }

    public int getNguongPhut() {
        return nguongPhut;
    }

    /** Phân loại mức độ dựa trên số phút đã trôi qua kể từ giờ bắt đầu của việc. */
    public static MucDoCanhBao phanLoai(long soPhutDaTroiQua) {
        if (soPhutDaTroiQua >= TRE_NANG.nguongPhut) return TRE_NANG;
        if (soPhutDaTroiQua >= TRE_NHE.nguongPhut) return TRE_NHE;
        return MOI_BAT_DAU;
    }
}
