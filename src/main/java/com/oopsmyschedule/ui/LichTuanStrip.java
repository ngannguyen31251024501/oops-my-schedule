package com.oopsmyschedule.ui;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Consumer;

/**
 * Thanh chọn ngày kiểu "tuần" — 1 hàng T2..CN với số ngày, có thể bấm chọn
 * trực tiếp (giống bản phác thảo giao diện của nhóm), thay cho cặp nút
 * ◀ ▶ đơn điệu trước đây. ◀ ▶ ở 2 đầu để lật sang tuần trước/sau; ngày đang
 * xem tô tròn xanh dương đặc, "hôm nay" (nếu khác ngày đang xem) viền xanh.
 */
public class LichTuanStrip extends JPanel {

    private static final String[] TEN_THU = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    private final JPanel hangNgay = new JPanel(new GridLayout(1, 7, 4, 0));
    private LocalDate dauTuanDangHien;
    private LocalDate ngayDangChon;
    private LocalDate homNay;
    private Consumer<LocalDate> khiChonNgay;

    public LichTuanStrip() {
        setOpaque(false);
        setLayout(new BorderLayout(4, 0));

        NutTron lui = new NutTron("‹");
        lui.addActionListener(e -> { dauTuanDangHien = dauTuanDangHien.minusWeeks(1); veLai(); });
        NutTron toi = new NutTron("›");
        toi.addActionListener(e -> { dauTuanDangHien = dauTuanDangHien.plusWeeks(1); veLai(); });

        hangNgay.setOpaque(false);
        add(lui, BorderLayout.WEST);
        add(hangNgay, BorderLayout.CENTER);
        add(toi, BorderLayout.EAST);
    }

    public void datHanhDongChonNgay(Consumer<LocalDate> callback) {
        this.khiChonNgay = callback;
    }

    /** Cập nhật ngày đang được chọn + "hôm nay" (để tô 2 kiểu khác nhau), tự lật tuần nếu ngày chọn ở tuần khác. */
    public void capNhat(LocalDate ngayDangChon, LocalDate homNay) {
        this.ngayDangChon = ngayDangChon;
        this.homNay = homNay;
        this.dauTuanDangHien = dauTuanCua(ngayDangChon);
        veLai();
    }

    private static LocalDate dauTuanCua(LocalDate ngay) {
        return ngay.getDayOfWeek() == DayOfWeek.MONDAY ? ngay
            : ngay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private void veLai() {
        hangNgay.removeAll();
        for (int i = 0; i < 7; i++) {
            LocalDate ngay = dauTuanDangHien.plusDays(i);
            hangNgay.add(taoONgay(ngay));
        }
        hangNgay.revalidate();
        hangNgay.repaint();
    }

    private JPanel taoONgay(LocalDate ngay) {
        boolean dangChon = ngay.equals(ngayDangChon);
        boolean laHomNay = ngay.equals(homNay);

        JPanel cot = new JPanel();
        cot.setOpaque(false);
        cot.setLayout(new BoxLayout(cot, BoxLayout.Y_AXIS));

        JLabel thu = new JLabel(TEN_THU[ngay.getDayOfWeek().getValue() - 1]);
        thu.setFont(VintageTheme.FONT_PHU_DE.deriveFont(11f));
        thu.setForeground(VintageTheme.CHU_MO);
        thu.setAlignmentX(Component.CENTER_ALIGNMENT);
        thu.setHorizontalAlignment(SwingConstants.CENTER);

        OSo so = new OSo(String.valueOf(ngay.getDayOfMonth()), dangChon, laHomNay);
        so.setAlignmentX(Component.CENTER_ALIGNMENT);
        so.addActionListener(e -> { if (khiChonNgay != null) khiChonNgay.accept(ngay); });

        cot.add(thu);
        cot.add(Box.createVerticalStrut(4));
        cot.add(so);
        return cot;
    }

    /** Số ngày tròn kiểu "chip" — nền xanh đặc nếu đang chọn, viền xanh nếu là hôm nay (mà chưa được chọn). */
    private static class OSo extends JButton {
        private final boolean dangChon;
        private final boolean laHomNay;

        OSo(String text, boolean dangChon, boolean laHomNay) {
            super(text);
            this.dangChon = dangChon;
            this.laHomNay = laHomNay;
            setPreferredSize(new Dimension(34, 34));
            setFont(VintageTheme.FONT_NHAN.deriveFont(13f));
            setForeground(dangChon ? Color.WHITE : VintageTheme.CHU_CHINH);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - d) / 2, y = (getHeight() - d) / 2;
            if (dangChon) {
                g2.setColor(VintageTheme.XANH_DUONG);
                g2.fillOval(x, y, d, d);
            } else if (laHomNay) {
                g2.setColor(VintageTheme.XANH_DUONG);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawOval(x + 1, y + 1, d - 2, d - 2);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
