package com.oopsmyschedule.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Nút bo góc thay cho {@link JButton} vuông vức mặc định — đổi màu mượt khi
 * rê chuột (nội suy dần qua {@link Timer}, không giật cục) và nhún nhẹ 1-2px
 * khi nhấn, giúp giao diện "sinh động" hơn theo đúng tinh thần bo tròn/pastel
 * của linh vật mèo. Có 2 kiểu:
 * <ul>
 *   <li>{@link Kieu#CHINH}: nền đặc 1 màu, chữ trắng — dùng cho hành động
 *       chính (Xác nhận, Điều chỉnh lịch, + Thêm việc...).</li>
 *   <li>{@link Kieu#PHU}: nền trắng, viền + chữ theo màu — dùng cho hành
 *       động phụ (Hủy, Đóng, Sửa...).</li>
 * </ul>
 * Ngoài ra hỗ trợ {@link #datHieuUngNhay(boolean)} để icon tự nảy nhẹ lên
 * xuống liên tục — dùng cho chuông thông báo khi có tin chưa đọc.
 */
public class NutTron extends JButton {

    public enum Kieu { CHINH, PHU }

    private final Kieu kieu;
    private final Color mauGoc;
    private final Color mauHover;
    private final Color mauChuGoc;

    private float tienDoHover = 0f; // 0 = bình thường, 1 = hover đầy đủ
    private Timer timerHover;
    private boolean dangNhan = false;

    private Timer timerNhay;
    private float phaNhay = 0f;
    private float lechNhay = 0f;

    public NutTron(String text) {
        this(text, Kieu.PHU, VintageTheme.XANH_DUONG);
    }

    public NutTron(String text, Kieu kieu) {
        this(text, kieu, VintageTheme.XANH_DUONG);
    }

    public NutTron(String text, Kieu kieu, Color mauNhan) {
        super(text);
        this.kieu = kieu;
        if (kieu == Kieu.CHINH) {
            this.mauGoc = mauNhan;
            this.mauHover = mauNhan.darker();
            this.mauChuGoc = Color.WHITE;
        } else {
            this.mauGoc = VintageTheme.NEN_THE;
            this.mauHover = VintageTheme.nhat(mauNhan, 0.85f);
            this.mauChuGoc = mauNhan.darker();
        }

        setFont(VintageTheme.FONT_NHAN);
        setForeground(mauChuGoc);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(SwingConstants.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { chayHieuUngHover(); }
            @Override
            public void mouseExited(MouseEvent e) { chayHieuUngHover(); }
            @Override
            public void mousePressed(MouseEvent e) { dangNhan = true; repaint(); }
            @Override
            public void mouseReleased(MouseEvent e) { dangNhan = false; repaint(); }
        });
    }

    private void chayHieuUngHover() {
        if (timerHover != null && timerHover.isRunning()) return;
        timerHover = new Timer(15, e -> {
            boolean qua = getModel().isRollover();
            float dich = qua ? 0.18f : -0.18f;
            tienDoHover = Math.max(0f, Math.min(1f, tienDoHover + dich));
            repaint();
            if ((qua && tienDoHover >= 1f) || (!qua && tienDoHover <= 0f)) {
                ((Timer) e.getSource()).stop();
            }
        });
        timerHover.start();
    }

    /** Bật/tắt hiệu ứng "nảy nhẹ" liên tục — dùng cho icon cần thu hút chú ý (VD: chuông có thông báo mới). */
    public void datHieuUngNhay(boolean bat) {
        if (bat) {
            if (timerNhay != null && timerNhay.isRunning()) return;
            timerNhay = new Timer(40, e -> {
                phaNhay += 0.25f;
                lechNhay = (float) Math.sin(phaNhay) * 2.6f;
                repaint();
            });
            timerNhay.start();
        } else {
            if (timerNhay != null) timerNhay.stop();
            lechNhay = 0f;
            repaint();
        }
    }

    private static Color trongDoi(Color a, Color b, float t) {
        int r = Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color mauVe = trongDoi(mauGoc, mauHover, tienDoHover);
        int nhun = dangNhan ? 2 : 0;
        float dichY = nhun + lechNhay;

        Graphics2D gNen = (Graphics2D) g.create();
        gNen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gNen.translate(0, dichY);
        int w = getWidth();
        int h = getHeight() - nhun;
        int bk = 14;
        gNen.setColor(mauVe);
        gNen.fillRoundRect(0, 0, w, h, bk, bk);
        if (kieu == Kieu.PHU) {
            gNen.setColor(mauChuGoc);
            gNen.setStroke(new BasicStroke(1.6f));
            gNen.drawRoundRect(1, 1, w - 3, h - 3, bk, bk);
        }
        gNen.dispose();

        Graphics2D gChu = (Graphics2D) g.create();
        gChu.translate(0, dichY);
        super.paintComponent(gChu);
        gChu.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 44), Math.max(d.height, 34));
    }
}
