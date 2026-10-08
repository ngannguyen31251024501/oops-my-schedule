package com.oopsmyschedule.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.util.Random;

/**
 * Bầu trời hoạt hình đổi màu + hiệu ứng theo buổi trong ngày (sáng / trưa /
 * chiều / tối / đêm khuya) — dùng làm nền cho header "Trang chủ".
 * <p>
 * Vẽ hoàn toàn bằng Graphics2D (mặt trời/mặt trăng/mây/sao đều là hình khối
 * tự vẽ, cùng tông màu với {@link VintageTheme} và nét viền giống
 * {@link MascotArt}) — KHÔNG dùng ảnh tải từ nguồn ngoài nào, nên không có
 * rủi ro bản quyền. Mây trôi nhẹ + sao lấp lánh nhờ 1 {@link Timer} hoạt hình
 * chạy riêng, độc lập với nhịp đồng hồ chính của app.
 */
public class SkyPanel extends JPanel {

    private enum Buoi { SANG, TRUA, CHIEU, TOI, DEM }

    private Buoi buoiHienTai = Buoi.SANG;
    private float troiOffset = 0f;
    private final float[] phaSao;
    private final Timer timerHoatHinh;

    public SkyPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(100, 118));

        Random rd = new Random(7); // seed cố định để bố cục sao không "nhảy lung tung" mỗi lần vẽ lại
        phaSao = new float[24];
        for (int i = 0; i < phaSao.length; i++) phaSao[i] = rd.nextFloat() * 6.28f;

        timerHoatHinh = new Timer(70, e -> {
            troiOffset += 0.6f;
            if (troiOffset > 100000f) troiOffset = 0f;
            repaint();
        });
        timerHoatHinh.start();
    }

    /** Gọi mỗi khi MainFrame cập nhật giờ — đổi bảng màu/vị trí mặt trời-mặt trăng theo buổi hiện tại. */
    public void capNhatTheoGio(LocalTime hienTai) {
        int gio = hienTai.getHour();
        Buoi moi;
        if (gio >= 5 && gio < 11) moi = Buoi.SANG;
        else if (gio >= 11 && gio < 14) moi = Buoi.TRUA;
        else if (gio >= 14 && gio < 18) moi = Buoi.CHIEU;
        else if (gio >= 18 && gio < 22) moi = Buoi.TOI;
        else moi = Buoi.DEM;
        if (moi != buoiHienTai) {
            buoiHienTai = moi;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        int bankinh = 22;
        Shape khungBo = new RoundRectangle2D.Float(0, 0, w, h, bankinh, bankinh);
        g2.setClip(khungBo);

        veBauTroi(g2, w, h);
        if (buoiHienTai != Buoi.DEM) veMay(g2, w, h, 0.55f);
        if (buoiHienTai == Buoi.TOI || buoiHienTai == Buoi.DEM) veSao(g2, w, h);
        veThienThe(g2, w, h);
        if (buoiHienTai == Buoi.DEM) veMay(g2, w, h, 0.16f); // mây đêm mờ nhạt, vẫn phía trước sao 1 chút

        g2.setClip(null);
        g2.setColor(VintageTheme.VIEN);
        g2.setStroke(new BasicStroke(2.2f));
        g2.draw(new RoundRectangle2D.Float(1.1f, 1.1f, w - 2.2f, h - 2.2f, bankinh, bankinh));
        g2.dispose();
    }

    private void veBauTroi(Graphics2D g2, int w, int h) {
        Color tren, duoi;
        switch (buoiHienTai) {
            case SANG -> { tren = new Color(0xFF, 0xE6, 0xC9); duoi = new Color(0xCB, 0xE9, 0xFB); }
            case TRUA -> { tren = new Color(0x9E, 0xD7, 0xF9); duoi = new Color(0xEA, 0xF7, 0xFF); }
            case CHIEU -> { tren = new Color(0xFF, 0xCE, 0xA3); duoi = new Color(0xFF, 0xE9, 0xCE); }
            case TOI -> { tren = new Color(0x8C, 0x84, 0xCE); duoi = new Color(0xF0, 0xAB, 0x9B); }
            default -> { tren = new Color(0x30, 0x33, 0x5C); duoi = new Color(0x54, 0x58, 0x8C); }
        }
        g2.setPaint(new GradientPaint(0, 0, tren, 0, h, duoi));
        g2.fillRect(0, 0, w, h);
    }

    private void veThienThe(Graphics2D g2, int w, int h) {
        float x, y, bk;
        Color mau;
        switch (buoiHienTai) {
            case SANG -> { x = w * 0.18f; y = h * 0.58f; bk = 24; mau = new Color(0xF7, 0xC8, 0x73); }
            case TRUA -> { x = w * 0.50f; y = h * 0.20f; bk = 22; mau = new Color(0xFF, 0xDB, 0x5C); }
            case CHIEU -> { x = w * 0.80f; y = h * 0.46f; bk = 25; mau = new Color(0xF3, 0x95, 0x6B); }
            case TOI -> { x = w * 0.84f; y = h * 0.68f; bk = 21; mau = new Color(0xE8, 0x7A, 0x6A); }
            default -> { x = w * 0.78f; y = h * 0.26f; bk = 19; mau = new Color(0xF5, 0xE9, 0xC8); }
        }
        // hào quang mờ xung quanh, hơi "thở" nhẹ theo thời gian cho có sức sống
        float tho = 1f + 0.06f * (float) Math.sin(troiOffset * 0.03f);
        g2.setColor(new Color(mau.getRed(), mau.getGreen(), mau.getBlue(), 65));
        float hq = bk * 1.7f * tho;
        g2.fill(new Ellipse2D.Float(x - hq, y - hq, hq * 2, hq * 2));

        if (buoiHienTai == Buoi.DEM) {
            g2.setColor(mau);
            Ellipse2D.Float tron = new Ellipse2D.Float(x - bk, y - bk, bk * 2, bk * 2);
            g2.fill(tron);
            g2.setColor(new Color(0x20, 0x1C, 0x3B));
            g2.fill(new Ellipse2D.Float(x - bk + bk * 0.65f, y - bk, bk * 2, bk * 2));
            g2.setColor(VintageTheme.VIEN);
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(tron);
        } else {
            Ellipse2D.Float tron = new Ellipse2D.Float(x - bk, y - bk, bk * 2, bk * 2);
            g2.setColor(mau);
            g2.fill(tron);
            g2.setColor(VintageTheme.VIEN);
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(tron);
        }
    }

    private void veSao(Graphics2D g2, int w, int h) {
        for (int i = 0; i < phaSao.length; i++) {
            float sx = (i * 41 % Math.max(w, 1));
            float sy = (i * 29 % Math.max((int) (h * 0.6f), 1)) + 4;
            float nhap = (float) (0.35 + 0.65 * Math.abs(Math.sin(troiOffset * 0.035f + phaSao[i])));
            g2.setColor(new Color(1f, 1f, 0.92f, nhap));
            g2.fill(new Ellipse2D.Float(sx, sy, 2.6f, 2.6f));
        }
    }

    private void veMay(Graphics2D g2, int w, int h, float doMo) {
        int alpha = Math.round(255 * doMo);
        Color mauMay = new Color(255, 255, 255, alpha);
        float chuKy = w + 180;
        veCumMay(g2, ((troiOffset) % chuKy) - 90, h * 0.30f, 28, mauMay);
        veCumMay(g2, ((troiOffset * 0.6f + 240) % chuKy) - 100, h * 0.48f, 20, mauMay);
        veCumMay(g2, ((troiOffset * 0.4f + 420) % chuKy) - 110, h * 0.16f, 17, mauMay);
    }

    private void veCumMay(Graphics2D g2, float x, float y, float bk, Color mau) {
        g2.setColor(mau);
        g2.fill(new Ellipse2D.Float(x, y, bk * 1.6f, bk));
        g2.fill(new Ellipse2D.Float(x + bk * 0.8f, y - bk * 0.35f, bk * 1.3f, bk));
        g2.fill(new Ellipse2D.Float(x + bk * 1.5f, y, bk * 1.4f, bk * 0.85f));
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        timerHoatHinh.stop();
    }
}
