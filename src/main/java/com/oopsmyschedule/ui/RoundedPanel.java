package com.oopsmyschedule.ui;

import javax.swing.*;
import java.awt.*;

/**
 * JPanel bo góc dùng chung cho mọi card/khung trong app — thay cho JPanel
 * vuông vức mặc định, giúp giao diện mềm mại và đồng bộ phong cách vintage
 * hơn. Vẽ nền + viền (nếu có) bằng Graphics2D, con bên trong đặt layout bình
 * thường như JPanel thường.
 */
public class RoundedPanel extends JPanel {

    private final int bankinh;
    private Color mauVien;
    private float doDayVien = 1.6f;

    public RoundedPanel(int bankinh) {
        this.bankinh = bankinh;
        setOpaque(false);
    }

    public RoundedPanel(int bankinh, Color mauVien) {
        this(bankinh);
        this.mauVien = mauVien;
    }

    public RoundedPanel(int bankinh, Color mauVien, float doDayVien) {
        this(bankinh, mauVien);
        this.doDayVien = doDayVien;
    }

    public void datMauVien(Color mauVien) {
        this.mauVien = mauVien;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, bankinh, bankinh);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (mauVien == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(mauVien);
        g2.setStroke(new BasicStroke(doDayVien));
        float nua = doDayVien / 2f;
        g2.draw(new java.awt.geom.RoundRectangle2D.Float(nua, nua,
            getWidth() - 1 - doDayVien, getHeight() - 1 - doDayVien, bankinh, bankinh));
        g2.dispose();
    }
}
