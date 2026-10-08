package com.oopsmyschedule.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * Linh vật mèo — vẽ hoàn toàn bằng hình khối (oval/polygon) đối xứng thay vì
 * dò từng pixel ASCII, để đảm bảo tỉ lệ luôn đẹp ở mọi kích thước. Tắt
 * anti-alias ở viền để giữ cảm giác "pixel/vintage" hơi thô mộc, giống
 * Duolingo nhưng blocky hơn.
 */
public final class MascotArt {
    private MascotArt() {}

    public enum BieuCam { BINH_THUONG, VUI, OOPS, NGU }

    private static final Color LONG_MEO = new Color(0xFF, 0xFF, 0xFF); // trắng, đồng bộ tông "trắng + xanh dương"
    private static final Color TAI_MA_HONG = new Color(0xFF, 0x9F, 0xC7); // hồng pastel
    private static final Color VIEN = new Color(0x27, 0x3B, 0x5C); // xanh than đậm, đồng bộ VintageTheme.VIEN
    private static final Color MA = new Color(0xFF, 0x9F, 0xC7);

    /** Vẽ mèo trong khung vuông cạnh {@code kichThuoc}, góc trên-trái tại (x,y). */
    public static void ve(Graphics2D g2, int x, int y, int kichThuoc, BieuCam bieuCam) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.translate(x, y);
        double s = kichThuoc / 100.0; // hệ số scale, thiết kế gốc theo lưới 100x100
        g.scale(s, s);
        g.setStroke(new BasicStroke(3f));

        // --- Tai ---
        ve2Tai(g);

        // --- Đầu ---
        Ellipse2D dau = new Ellipse2D.Double(8, 22, 84, 72);
        g.setColor(LONG_MEO);
        g.fill(dau);
        g.setColor(VIEN);
        g.draw(dau);

        // --- Má hồng ---
        g.setColor(MA);
        g.fill(new Ellipse2D.Double(14, 62, 16, 10));
        g.fill(new Ellipse2D.Double(70, 62, 16, 10));

        // --- Mắt + miệng theo biểu cảm ---
        switch (bieuCam) {
            case VUI -> veMatVui(g);
            case OOPS -> veMatOops(g);
            case NGU -> veMatNgu(g);
            default -> veMatBinhThuong(g);
        }

        // --- Mũi ---
        g.setColor(VIEN);
        Path2D mui = new Path2D.Double();
        mui.moveTo(45, 66);
        mui.lineTo(55, 66);
        mui.lineTo(50, 72);
        mui.closePath();
        g.fill(mui);

        g.dispose();
    }

    private static void ve2Tai(Graphics2D g) {
        Path2D taiTrai = new Path2D.Double();
        taiTrai.moveTo(10, 30);
        taiTrai.lineTo(20, 2);
        taiTrai.lineTo(42, 26);
        taiTrai.closePath();
        Path2D taiPhai = new Path2D.Double();
        taiPhai.moveTo(90, 30);
        taiPhai.lineTo(80, 2);
        taiPhai.lineTo(58, 26);
        taiPhai.closePath();

        g.setColor(LONG_MEO);
        g.fill(taiTrai);
        g.fill(taiPhai);
        g.setColor(VIEN);
        g.draw(taiTrai);
        g.draw(taiPhai);

        // Tai trong hồng
        Path2D taiTrongTrai = new Path2D.Double();
        taiTrongTrai.moveTo(15, 26);
        taiTrongTrai.lineTo(21, 10);
        taiTrongTrai.lineTo(33, 24);
        taiTrongTrai.closePath();
        Path2D taiTrongPhai = new Path2D.Double();
        taiTrongPhai.moveTo(85, 26);
        taiTrongPhai.lineTo(79, 10);
        taiTrongPhai.lineTo(67, 24);
        taiTrongPhai.closePath();
        g.setColor(TAI_MA_HONG);
        g.fill(taiTrongTrai);
        g.fill(taiTrongPhai);
    }

    private static void veMatBinhThuong(Graphics2D g) {
        g.setColor(VIEN);
        g.fill(new Ellipse2D.Double(28, 48, 10, 12));
        g.fill(new Ellipse2D.Double(62, 48, 10, 12));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(31, 50, 3, 3));
        g.fill(new Ellipse2D.Double(65, 50, 3, 3));
        g.setColor(VIEN);
        g.draw(cung(46, 78, 8, 6));
    }

    private static void veMatVui(Graphics2D g) {
        g.setColor(VIEN);
        g.setStroke(new BasicStroke(3.5f));
        g.draw(cungLat(26, 48, 12, 10)); // mắt cong lên = ^_^
        g.draw(cungLat(60, 48, 12, 10));
        g.draw(cungRong(40, 70, 20, 14)); // miệng cười rộng
    }

    private static void veMatOops(Graphics2D g) {
        g.setColor(VIEN);
        g.fill(new Ellipse2D.Double(26, 45, 14, 16)); // mắt to tròn = giật mình
        g.fill(new Ellipse2D.Double(60, 45, 14, 16));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(30, 47, 4, 4));
        g.fill(new Ellipse2D.Double(64, 47, 4, 4));
        g.setColor(VIEN);
        g.fill(new Ellipse2D.Double(44, 76, 12, 10)); // miệng "o" tròn
    }

    private static void veMatNgu(Graphics2D g) {
        g.setColor(VIEN);
        g.setStroke(new BasicStroke(3.5f));
        g.drawLine(26, 52, 38, 52); // mắt nhắm = 2 gạch ngang
        g.drawLine(60, 52, 72, 52);
        g.draw(cung(46, 76, 8, 5));
        // "z z z" ngủ
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        g.drawString("z", 82, 20);
        g.drawString("z", 90, 10);
    }

    private static Path2D cung(double cx, double cy, double rx, double ry) {
        Path2D p = new Path2D.Double();
        p.moveTo(cx - rx, cy);
        p.quadTo(cx, cy + ry, cx + rx, cy);
        return p;
    }

    private static Path2D cungLat(double cx, double cy, double rx, double ry) {
        Path2D p = new Path2D.Double();
        p.moveTo(cx - rx / 2, cy + ry);
        p.quadTo(cx, cy - ry, cx + rx / 2, cy + ry);
        return p;
    }

    private static Path2D cungRong(double cx, double cy, double rx, double ry) {
        Path2D p = new Path2D.Double();
        p.moveTo(cx - rx / 2, cy);
        p.quadTo(cx, cy + ry, cx + rx / 2, cy);
        return p;
    }
}
