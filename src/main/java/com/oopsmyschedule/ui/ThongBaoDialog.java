package com.oopsmyschedule.ui;

import com.oopsmyschedule.service.ThongBao;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Dialog "🔔 Thông báo" — liệt kê lại mọi lời nhắc app đã từng gửi, mới nhất lên đầu. */
public class ThongBaoDialog extends JDialog {

    private static final DateTimeFormatter GIO_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public ThongBaoDialog(Frame parent, List<ThongBao> danhSach) {
        super(parent, "🔔 Thông báo", true);
        getContentPane().setBackground(VintageTheme.NEN_CHINH);
        setLayout(new BorderLayout(0, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel tieuDe = new JLabel("🔔 Thông báo gần đây");
        tieuDe.setFont(VintageTheme.FONT_TIEU_DE);
        tieuDe.setForeground(VintageTheme.CHU_CHINH);
        add(tieuDe, BorderLayout.NORTH);

        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setOpaque(false);

        if (danhSach.isEmpty()) {
            JLabel trong = new JLabel("Chưa có thông báo nào cả — mọi thứ đang yên bình 🐾");
            trong.setForeground(VintageTheme.CHU_MO);
            trong.setBorder(BorderFactory.createEmptyBorder(24, 4, 24, 4));
            noiDung.add(trong);
        } else {
            for (ThongBao tb : danhSach) {
                noiDung.add(taoThe(tb));
                noiDung.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane scroll = new JScrollPane(noiDung);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(460, 420));
        add(scroll, BorderLayout.CENTER);

        NutTron dong = new NutTron("Đóng");
        dong.addActionListener(e -> setVisible(false));
        JPanel duoi = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        duoi.setOpaque(false);
        duoi.add(dong);
        add(duoi, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private RoundedPanel taoThe(ThongBao tb) {
        RoundedPanel the = new RoundedPanel(12, VintageTheme.VIEN, 1f);
        the.setBackground(VintageTheme.NEN_THE);
        the.setLayout(new BorderLayout(10, 0));
        the.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        the.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel gioLabel = new JLabel(tb.getGio().format(GIO_FMT));
        gioLabel.setFont(VintageTheme.FONT_NHAN);
        gioLabel.setForeground(VintageTheme.CHU_MO);
        gioLabel.setVerticalAlignment(SwingConstants.TOP);
        the.add(gioLabel, BorderLayout.WEST);

        JLabel noiDung = new JLabel("<html><body style='width:340px'>" + tb.getNoiDung() + "</body></html>");
        noiDung.setFont(VintageTheme.FONT_PHU_DE);
        noiDung.setForeground(VintageTheme.CHU_CHINH);
        JPanel bocNgoai = new JPanel(new BorderLayout());
        bocNgoai.setOpaque(false);
        bocNgoai.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        bocNgoai.add(noiDung, BorderLayout.CENTER);
        the.add(bocNgoai, BorderLayout.CENTER);

        return the;
    }
}
