package com.oopsmyschedule.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Toast thông báo nhỏ gọn, tự biến mất sau vài giây — dùng thay cho
 * {@link JOptionPane} ở những chỗ chỉ cần XÁC NHẬN thao tác đã thành công
 * (thêm/sửa/xóa việc, áp dụng phương án điều chỉnh...), vì JOptionPane bắt
 * người dùng phải bấm "OK" mỗi lần, khá phiền nếu thao tác liên tục.
 * <p>
 * Cố tình không dùng thư viện ngoài — chỉ 1 {@link JWindow} (không viền) +
 * {@link Timer} để tự đóng, đủ dùng cho 1 đồ án Swing thuần.
 */
public final class Toast {

    public enum Loai {
        THANH_CONG(new Color(0x2E, 0x7D, 0x32), "✓"),
        LOI(new Color(0xC6, 0x28, 0x28), "⚠");

        final Color mau;
        final String icon;

        Loai(Color mau, String icon) {
            this.mau = mau;
            this.icon = icon;
        }
    }

    private static JWindow dangHien; // chỉ giữ 1 toast tại 1 thời điểm, tránh chồng chất khi bấm nhanh tay

    private Toast() {
    }

    public static void thanhCong(Window chuSoHuu, String tieuDe, String moTa) {
        hien(chuSoHuu, tieuDe, moTa, Loai.THANH_CONG);
    }

    public static void loi(Window chuSoHuu, String tieuDe, String moTa) {
        hien(chuSoHuu, tieuDe, moTa, Loai.LOI);
    }

    /**
     * FIX (feedback UX 2.1): thay vì popup "Bạn có chắc chắn?" mỗi lần bấm
     * "Xong" (làm chậm thao tác vốn rất thường xuyên), hiện toast xác nhận
     * kèm nút "Hoàn tác" trong vài giây — vừa nhanh vừa chống bấm nhầm.
     */
    public static void thanhCongCoHoanTac(Window chuSoHuu, String tieuDe, Runnable khiHoanTac) {
        hienNoiBo(chuSoHuu, tieuDe, null, Loai.THANH_CONG, khiHoanTac);
    }

    public static void hien(Window chuSoHuu, String tieuDe, String moTa, Loai loai) {
        hienNoiBo(chuSoHuu, tieuDe, moTa, loai, null);
    }

    private static void hienNoiBo(Window chuSoHuu, String tieuDe, String moTa, Loai loai, Runnable khiHoanTac) {
        if (chuSoHuu == null || !chuSoHuu.isShowing()) {
            return; // không có cửa sổ chủ hợp lệ để neo vị trí -> bỏ qua thay vì crash
        }
        if (dangHien != null) {
            dangHien.dispose();
        }

        JWindow w = new JWindow(chuSoHuu);
        RoundedPanel the = new RoundedPanel(14);
        the.setBackground(loai.mau);
        the.setLayout(new BorderLayout(10, 2));
        the.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 18));

        JLabel icon = new JLabel(loai.icon);
        icon.setFont(new Font("SansSerif", Font.BOLD, 20));
        icon.setForeground(Color.WHITE);
        icon.setVerticalAlignment(SwingConstants.TOP);
        the.add(icon, BorderLayout.WEST);

        JPanel chu = new JPanel();
        chu.setOpaque(false);
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
        JLabel dong1 = new JLabel(tieuDe);
        dong1.setFont(VintageTheme.FONT_NHAN.deriveFont(Font.BOLD, 13f));
        dong1.setForeground(Color.WHITE);
        dong1.setAlignmentX(Component.LEFT_ALIGNMENT);
        chu.add(dong1);
        if (moTa != null && !moTa.isBlank()) {
            JLabel dong2 = new JLabel("<html><body style='width:250px'>" + moTa + "</body></html>");
            dong2.setFont(VintageTheme.FONT_PHU_DE.deriveFont(11.5f));
            dong2.setForeground(new Color(255, 255, 255, 235));
            dong2.setAlignmentX(Component.LEFT_ALIGNMENT);
            chu.add(Box.createVerticalStrut(3));
            chu.add(dong2);
        }
        the.add(chu, BorderLayout.CENTER);

        if (khiHoanTac != null) {
            JButton nutHoanTac = new JButton("Hoàn tác");
            nutHoanTac.setFocusPainted(false);
            nutHoanTac.setForeground(Color.WHITE);
            nutHoanTac.setBackground(new Color(255, 255, 255, 40));
            nutHoanTac.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            nutHoanTac.setFont(VintageTheme.FONT_NHAN.deriveFont(Font.BOLD, 12f));
            nutHoanTac.addActionListener(e -> {
                khiHoanTac.run();
                w.dispose();
                if (dangHien == w) {
                    dangHien = null;
                }
            });
            the.add(nutHoanTac, BorderLayout.EAST);
        }

        w.setLayout(new BorderLayout());
        w.add(the);
        w.pack();
        if (w.getWidth() > 340) {
            the.setPreferredSize(new Dimension(340, the.getPreferredSize().height));
            w.pack();
        }

        Point diem = chuSoHuu.getLocationOnScreen();
        int x = diem.x + chuSoHuu.getWidth() - w.getWidth() - 28;
        int y = diem.y + 56;
        w.setLocation(x, y);
        w.setVisible(true);
        dangHien = w;

        Timer dong = new Timer(3200, e -> {
            w.dispose();
            if (dangHien == w) {
                dangHien = null;
            }
        });
        dong.setRepeats(false);
        dong.start();
    }
}
