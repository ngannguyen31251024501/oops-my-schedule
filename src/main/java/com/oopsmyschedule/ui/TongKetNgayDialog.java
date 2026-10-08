package com.oopsmyschedule.ui;

import com.oopsmyschedule.service.SuCoNhatKy;
import com.oopsmyschedule.service.TongKetNgay;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * "📊 Tổng kết ngày" — hiển thị {@link TongKetNgay}: 2 thanh tiến độ (theo số
 * lượng + theo khối lượng thời gian), 4 con số (hoàn thành/trễ/dời/hủy), và
 * vài dòng lời khuyên rule-based. Cố tình chỉ là 1 dialog đơn giản, không
 * phải dashboard nhiều biểu đồ — đúng tinh thần "Daily Summary" MVP.
 */
public class TongKetNgayDialog extends JDialog {

    public TongKetNgayDialog(Window owner, LocalDate ngay, TongKetNgay tk) {
        super(owner, "📊 Tổng kết ngày", ModalityType.APPLICATION_MODAL);
        setSize(420, 520);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(VintageTheme.NEN_CHINH);
        setLayout(new BorderLayout(0, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));

        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));

        JLabel tieuDe = new JLabel("📊 TỔNG KẾT NGÀY · " + ngay);
        tieuDe.setFont(VintageTheme.FONT_NHAN.deriveFont(Font.BOLD, 16f));
        tieuDe.setForeground(VintageTheme.CHU_CHINH);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDung.add(tieuDe);
        noiDung.add(Box.createVerticalStrut(12));

        noiDung.add(nhanMuc("🎯 Mức độ hoàn thành (theo số lượng việc)"));
        noiDung.add(thanhTienDo(tk.getPhanTramSoLuong(), VintageTheme.XANH_DUONG));
        noiDung.add(nhanPhu("Đã hoàn thành " + tk.getSoHoanThanh() + "/" + tk.getTongSoViec() + " hoạt động"));
        noiDung.add(Box.createVerticalStrut(10));

        noiDung.add(nhanMuc("⏱ Khối lượng công việc (theo thời lượng)"));
        noiDung.add(thanhTienDo(tk.getPhanTramKhoiLuong(), VintageTheme.XANH_MINT));
        noiDung.add(nhanPhu(tk.moTaKhoiLuong()));
        noiDung.add(Box.createVerticalStrut(14));

        noiDung.add(hangSo("✓ Hoàn thành", tk.getSoHoanThanh(), VintageTheme.XANH_MINT));
        noiDung.add(hangSo("⚠ Trễ", tk.getSoTre(), VintageTheme.NEN_LAM));
        noiDung.add(hangSo("↪ Đã dời", tk.getSoDaDoi(), VintageTheme.XANH_DUONG));
        noiDung.add(hangSo("🗑 Đã hủy", tk.getSoDaHuy(), VintageTheme.BAT_BUOC));
        noiDung.add(Box.createVerticalStrut(14));

        noiDung.add(nhanMuc("💡 NHẬN XÉT"));
        for (String loi : tk.getLoiKhuyen()) {
            JLabel dong = new JLabel("<html><body style='width:320px'>• " + loi + "</body></html>");
            dong.setFont(VintageTheme.FONT_PHU_DE);
            dong.setForeground(VintageTheme.CHU_CHINH);
            dong.setAlignmentX(Component.LEFT_ALIGNMENT);
            dong.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            noiDung.add(dong);
        }

        if (!tk.getNhatKySuCo().isEmpty()) {
            noiDung.add(Box.createVerticalStrut(14));
            noiDung.add(nhanMuc("🔎 Vì sao hôm nay chưa đạt"));
            for (SuCoNhatKy sc : tk.getNhatKySuCo()) {
                noiDung.add(theSuCo(sc));
                noiDung.add(Box.createVerticalStrut(6));
            }
            String nguyenNhan = tk.nguyenNhanChinh();
            if (nguyenNhan != null) {
                JLabel dong = new JLabel("<html><body style='width:320px'><b>💡 Nguyên nhân chính:</b> "
                    + nguyenNhan + "</body></html>");
                dong.setFont(VintageTheme.FONT_PHU_DE);
                dong.setForeground(VintageTheme.CHU_CHINH);
                dong.setAlignmentX(Component.LEFT_ALIGNMENT);
                dong.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
                noiDung.add(dong);
            }
        }

        JScrollPane scroll = new JScrollPane(noiDung);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        NutTron dong = new NutTron("Đóng", NutTron.Kieu.CHINH, VintageTheme.XANH_DUONG);
        dong.addActionListener(e -> dispose());
        JPanel duoi = new JPanel(new FlowLayout(FlowLayout.CENTER));
        duoi.setOpaque(false);
        duoi.add(dong);
        add(duoi, BorderLayout.SOUTH);
    }

    private JLabel nhanMuc(String text) {
        JLabel l = new JLabel(text);
        l.setFont(VintageTheme.FONT_NHAN.deriveFont(Font.BOLD, 13f));
        l.setForeground(VintageTheme.CHU_CHINH);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return l;
    }

    private JLabel nhanPhu(String text) {
        JLabel l = new JLabel(text);
        l.setFont(VintageTheme.FONT_PHU_DE);
        l.setForeground(VintageTheme.CHU_MO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        return l;
    }

    private JPanel thanhTienDo(double phanTram, Color mau) {
        JPanel nen = new RoundedPanel(8);
        nen.setLayout(null);
        nen.setBackground(VintageTheme.NEN_THE);
        nen.setAlignmentX(Component.LEFT_ALIGNMENT);
        nen.setMaximumSize(new Dimension(1000, 18));
        nen.setPreferredSize(new Dimension(360, 18));

        int rong = (int) Math.round(Math.max(0, Math.min(100, phanTram)) * 3.6);
        JPanel thanh = new RoundedPanel(8);
        thanh.setBackground(mau);
        thanh.setBounds(0, 0, Math.max(rong, 8), 18);
        nen.add(thanh);

        JLabel nhan = new JLabel(Math.round(phanTram) + "%");
        nhan.setFont(VintageTheme.FONT_PHU_DE.deriveFont(Font.BOLD, 11f));
        nhan.setForeground(VintageTheme.CHU_CHINH);
        nhan.setBounds(0, 0, 360, 18);
        nhan.setHorizontalAlignment(SwingConstants.RIGHT);
        nen.add(nhan);
        return nen;
    }

    private JPanel hangSo(String nhan, int giaTri, Color mau) {
        JPanel hang = new JPanel(new BorderLayout());
        hang.setOpaque(false);
        hang.setAlignmentX(Component.LEFT_ALIGNMENT);
        hang.setMaximumSize(new Dimension(1000, 24));
        JLabel trai = new JLabel(nhan);
        trai.setFont(VintageTheme.FONT_PHU_DE);
        trai.setForeground(VintageTheme.CHU_CHINH);
        JLabel phai = new JLabel(String.valueOf(giaTri));
        phai.setFont(VintageTheme.FONT_PHU_DE.deriveFont(Font.BOLD, 13f));
        phai.setForeground(mau);
        hang.add(trai, BorderLayout.WEST);
        hang.add(phai, BorderLayout.EAST);
        return hang;
    }

    /**
     * 1 thẻ nhỏ cho 1 sự cố trong nhật ký: tiêu đề sự cố + phương án đã chọn,
     * kèm danh sách thay đổi cụ thể (VD: "Làm bài tập → Dời sang 16:00") —
     * chính là liên kết trực tiếp lịch sử Replan Engine vào Tổng kết ngày.
     */
    private JPanel theSuCo(SuCoNhatKy sc) {
        RoundedPanel the = new RoundedPanel(12, VintageTheme.NEN_LAM, 1.2f);
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBackground(VintageTheme.nhat(VintageTheme.NEN_LAM, 0.92f));
        the.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel dongTieuDe = new JLabel("<html><body style='width:300px'>⚠️ [" + sc.getGio().withNano(0) + "] "
            + sc.getMoTaSuCo() + " — đã chọn <b>" + sc.getTenPhuongAn() + "</b></body></html>");
        dongTieuDe.setFont(VintageTheme.FONT_PHU_DE);
        dongTieuDe.setForeground(VintageTheme.CHU_CHINH);
        dongTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.add(dongTieuDe);

        for (String thayDoi : sc.getDanhSachThayDoiThucSu()) {
            JLabel dong = new JLabel("<html><body style='width:290px'>• " + thayDoi + "</body></html>");
            dong.setFont(VintageTheme.FONT_PHU_DE.deriveFont(11.5f));
            dong.setForeground(VintageTheme.CHU_MO);
            dong.setAlignmentX(Component.LEFT_ALIGNMENT);
            dong.setBorder(BorderFactory.createEmptyBorder(3, 8, 0, 0));
            the.add(dong);
        }
        if (!sc.getTenKhongTheSapXep().isEmpty()) {
            JLabel dong = new JLabel("<html><body style='width:290px'>• ❌ Chưa thể sắp xếp: "
                + String.join(", ", sc.getTenKhongTheSapXep()) + "</body></html>");
            dong.setFont(VintageTheme.FONT_PHU_DE.deriveFont(11.5f));
            dong.setForeground(VintageTheme.BAT_BUOC.darker());
            dong.setAlignmentX(Component.LEFT_ALIGNMENT);
            dong.setBorder(BorderFactory.createEmptyBorder(3, 8, 0, 0));
            the.add(dong);
        }
        return the;
    }
}
