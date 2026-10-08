package com.oopsmyschedule.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Bảng màu & font dùng chung cho toàn app — tông "trắng + xanh dương pastel,
 * nhiều màu sắc" lấy cảm hứng từ linh vật mèo trên ảnh mô tả đề tài: nền
 * trắng/xanh nhạt sạch sẽ, chữ xanh than đậm dễ đọc, điểm xuyết nhiều màu
 * pastel (xanh dương, mint, hồng, vàng, cam san hô) cho từng mảng chức năng.
 * (Tên lớp giữ nguyên "VintageTheme" để không phải sửa import ở khắp nơi.)
 */
public final class VintageTheme {
    private VintageTheme() {}

    // ---------- Nền & khung ----------
    public static final Color NEN_CHINH = new Color(0xEE, 0xF6, 0xFF); // nền toàn app: trắng ngả xanh dương rất nhạt
    public static final Color NEN_THE = new Color(0xFF, 0xFF, 0xFF);   // nền thẻ/card: trắng tinh
    public static final Color NEN_NHAT = new Color(0xE3, 0xF1, 0xFF);  // nền nhạt để nhấn (highlight), xanh dương pastel
    public static final Color VIEN = new Color(0x27, 0x3B, 0x5C);      // viền/chữ chính: xanh than đậm (thay cho nâu vintage cũ)
    public static final Color CHU_CHINH = new Color(0x27, 0x3B, 0x5C);
    public static final Color CHU_MO = new Color(0x86, 0x96, 0xB0);    // chữ phụ: xanh xám nhạt

    // ---------- Màu theo mức ưu tiên ----------
    public static final Color BAT_BUOC = new Color(0xFF, 0x6B, 0x6B);  // đỏ san hô — bắt buộc / khẩn cấp
    public static final Color NEN_LAM = new Color(0xFF, 0xC1, 0x4D);   // vàng cam ấm — nên làm
    public static final Color CO_THE_BO = new Color(0xB7, 0xC4, 0xDA); // xanh xám nhạt — có thể bỏ

    // ---------- Bảng màu pastel nhiều màu, dùng làm điểm nhấn ----------
    public static final Color XANH_MINT = new Color(0x5D, 0xD9, 0xB0);
    public static final Color XANH_DUONG = new Color(0x4F, 0x9C, 0xF2);      // xanh dương chủ đạo (nút chính, điểm nhấn)
    public static final Color XANH_DUONG_DAM = new Color(0x2F, 0x74, 0xC9); // xanh dương đậm hơn — hover/nhấn
    public static final Color HONG = new Color(0xFF, 0x9F, 0xC7);
    public static final Color VANG_KEM = new Color(0xDD, 0xEF, 0xFF);       // nền thẻ nổi bật — xanh dương pastel rất nhạt
    public static final Color TIM_NHAT = new Color(0xB3, 0xA6, 0xF0);
    public static final Color CAM_SAN_HO = new Color(0xFF, 0x93, 0x7A);

    public static final Font FONT_TIEU_DE = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_PHU_DE = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_NHAN = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_NOI_DUNG = new Font("SansSerif", Font.PLAIN, 13);

    public static Color mauTheoUuTien(com.oopsmyschedule.domain.PriorityLevel muc) {
        return switch (muc) {
            case BAT_BUOC -> BAT_BUOC;
            case NEN_LAM -> NEN_LAM;
            case CO_THE_BO -> CO_THE_BO;
        };
    }

    /** Phiên bản pha trắng của 1 màu — dùng làm nền nhạt (thẻ, banner) đồng bộ với màu ưu tiên/điểm nhấn tương ứng. */
    public static Color nhat(Color mau, float tyLePhaTrang) {
        int r = Math.round(mau.getRed() + (255 - mau.getRed()) * tyLePhaTrang);
        int g = Math.round(mau.getGreen() + (255 - mau.getGreen()) * tyLePhaTrang);
        int b = Math.round(mau.getBlue() + (255 - mau.getBlue()) * tyLePhaTrang);
        return new Color(r, g, b);
    }
}
